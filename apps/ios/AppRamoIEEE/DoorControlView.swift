import SwiftUI

// Delegate que permite conexões HTTPS mesmo com certificados caseiros/inválidos
class InsecureDelegate: NSObject, URLSessionDelegate {
    func urlSession(_ session: URLSession, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
        if let trust = challenge.protectionSpace.serverTrust {
            completionHandler(.useCredential, URLCredential(trust: trust))
        } else {
            completionHandler(.performDefaultHandling, nil)
        }
    }
}

struct DoorControlView: View {
    private let baseURL = Bundle.main.object(forInfoDictionaryKey: "DoorRelayBaseURL") as? String ?? "https://ramoieeeufjf.dpdns.org"
    private let apiKey = Bundle.main.object(forInfoDictionaryKey: "DoorRelayAPIKey") as? String ?? ""
    private let deviceId = Bundle.main.object(forInfoDictionaryKey: "DoorRelayDeviceID") as? String ?? "esp01"
    
    @State private var doorOpen: Bool? = nil
    @State private var lightOn: Bool? = nil
    @State private var loading = false
    @State private var errorMessage: String? = nil
    
    // Sessão configurada para IoT
    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 8
        config.waitsForConnectivity = false
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return URLSession(configuration: config, delegate: InsecureDelegate(), delegateQueue: nil)
    }()
    
    var body: some View {
        VStack(spacing: 24) {
            Text("Controle de porta e luz via servidor (relay).")
                .multilineTextAlignment(.center)
            
            Group {
                HStack {
                    Text("Porta:")
                        .bold()
                    Text(doorStatusText(doorOpen))
                        .foregroundColor(doorColor(doorOpen))
                }
                
                HStack {
                    Text("Luz:")
                        .bold()
                    Text(lightStatusText(lightOn))
                        .foregroundColor(lightColor(lightOn))
                }
            }
            .font(.title3)
            
            if loading {
                ProgressView("Atualizando...")
            }
            
            if let error = errorMessage {
                Text("Erro: \(error)")
                    .foregroundColor(.red)
                    .font(.caption)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal)
                    .onTapGesture {
                        // Clique para copiar erro se necessário
                        UIPasteboard.general.string = error
                    }
            }
            
            Spacer().frame(height: 20)
            
            // Botões de Ação
            Button("Abrir a Porta") {
                sendCommand(action: "door_on")
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            
            Button(lightOn == true ? "Apagar a Luz" : "Acender a Luz") {
                let action = (lightOn == true) ? "light_off" : "light_on"
                sendCommand(action: action)
            }
            .buttonStyle(.bordered)
            .controlSize(.large)
            
            Button("Atualizar status") {
                refreshStatus()
            }
            .padding(.top)
            
            Spacer()
            
            Image(systemName: "house.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 60, height: 60)
                .foregroundColor(.gray.opacity(0.5))
        }
        .padding()
        .onAppear {
            refreshStatus()
        }
    }
    
    // MARK: - Auxiliares de Texto
    func doorStatusText(_ state: Bool?) -> String {
        guard let s = state else { return "Desconhecida" }
        return s ? "Aberta" : "Fechada"
    }
    
    func lightStatusText(_ state: Bool?) -> String {
        guard let s = state else { return "Desconhecida" }
        return s ? "Ligada" : "Desligada"
    }
    
    func doorColor(_ state: Bool?) -> Color {
        guard let s = state else { return .gray }
        return s ? .green : .red
    }
    
    func lightColor(_ state: Bool?) -> Color {
        guard let s = state else { return .gray }
        return s ? .yellow : .primary
    }
    
    // MARK: - API
    func refreshStatus() {
        loading = true
        errorMessage = nil
        
        guard let url = URL(string: "\(baseURL)/status?device_id=\(deviceId)") else { return }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue(apiKey, forHTTPHeaderField: "X-API-KEY")
        
        session.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                loading = false
                
                if let error = error {
                    self.errorMessage = "Falha: \(error.localizedDescription)"
                    return
                }
                
                guard let data = data else {
                    self.errorMessage = "Sem dados"
                    return
                }
                
                do {
                    let status = try JSONDecoder().decode(DeviceStatus.self, from: data)
                    if let d = status.door { self.doorOpen = (d == 1) }
                    if let l = status.light { self.lightOn = (l == 1) }
                } catch {
                    self.errorMessage = "Erro JSON: \(error.localizedDescription)"
                }
            }
        }.resume()
    }
    
    func sendCommand(action: String) {
        loading = true
        errorMessage = nil
        
        // Atualização Otimista
        if action == "door_on" { doorOpen = true }
        if action == "door_off" { doorOpen = false }
        if action == "light_on" { lightOn = true }
        if action == "light_off" { lightOn = false }
        
        guard let url = URL(string: "\(baseURL)/send") else { return }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.setValue(apiKey, forHTTPHeaderField: "X-API-KEY")
        
        let payload = CommandPayload(device_id: deviceId, command: ActionCommand(action: action))
        request.httpBody = try? JSONEncoder().encode(payload)
        
        session.dataTask(with: request) { data, response, error in
            if let error = error {
                DispatchQueue.main.async {
                    self.errorMessage = "Erro comando: \(error.localizedDescription)"
                    self.loading = false
                }
                return
            }
            
            if let httpResponse = response as? HTTPURLResponse {
                if httpResponse.statusCode == 200 {
                    // Sucesso
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                        self.refreshStatus()
                    }
                } else {
                    // Se der erro (ex: 404), tenta ler a mensagem do servidor
                    let serverMsg = data.flatMap { String(data: $0, encoding: .utf8) } ?? "Sem detalhes"
                    
                    DispatchQueue.main.async {
                        self.errorMessage = "Erro \(httpResponse.statusCode): \(serverMsg)"
                        self.loading = false
                        // Reverte estado otimista em caso de erro
                        self.refreshStatus()
                    }
                }
            }
        }.resume()
    }
}
