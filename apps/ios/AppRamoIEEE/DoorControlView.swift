import SwiftUI
import FirebaseAuth

struct DoorControlView: View {
    private let baseURL =
        (Bundle.main.object(forInfoDictionaryKey: "DoorAPIBaseURL") as? String)
        ?? (Bundle.main.object(forInfoDictionaryKey: "DoorRelayBaseURL") as? String)
        ?? "https://ramoieeeufjf.dpdns.org"
    private let apiKey =
        (Bundle.main.object(forInfoDictionaryKey: "DoorAPIKey") as? String)
        ?? (Bundle.main.object(forInfoDictionaryKey: "DoorRelayAPIKey") as? String)
        ?? ""

    @State private var meetingStatus: MeetingStatusResponse?
    @State private var delayMinutes = "5"
    @State private var profileIndices = ""
    @State private var recurrence = "none"
    @State private var weekdays = ""
    @State private var cancelId = ""
    @State private var loading = false
    @State private var errorMessage: String?
    @State private var successMessage: String?

    private let session: URLSession = {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 8
        config.waitsForConnectivity = false
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        return URLSession(configuration: config)
    }()

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                Image(systemName: "house.fill")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 56, height: 56)
                    .foregroundColor(.gray.opacity(0.6))

                Text("Controle da sala")
                    .font(.title2)
                    .bold()

                if loading {
                    ProgressView("Atualizando...")
                }

                if let error = errorMessage {
                    Text("Erro: \(error)")
                        .font(.caption)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                        .onTapGesture {
                            UIPasteboard.general.string = error
                        }
                }

                if let success = successMessage {
                    Text(success)
                        .font(.caption)
                        .foregroundColor(.blue)
                        .multilineTextAlignment(.center)
                }

                GroupBox("Porta") {
                    Button("Abrir porta") {
                        openDoor()
                    }
                    .buttonStyle(.borderedProminent)
                    .controlSize(.large)
                    .disabled(loading)
                    .frame(maxWidth: .infinity)
                }

                meetingStatusBox
                scheduleBox
                cancelBox
            }
            .padding()
        }
        .navigationTitle("Sala")
        .onAppear {
            refreshMeetingStatus()
        }
    }

    private var meetingStatusBox: some View {
        GroupBox {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Modo reunião")
                        .font(.headline)
                    Spacer()
                    Button("Atualizar") {
                        refreshMeetingStatus()
                    }
                    .disabled(loading)
                }

                if let status = meetingStatus {
                    Text((status.active ?? false) ? "Ativo" : "Inativo")
                    Text("Agendamentos pendentes: \(status.pending_count ?? 0)")
                    Text("Relógio sincronizado: \(yesNo(status.time_synced))")

                    if status.active ?? false {
                        Text("Perfis ativos: \(status.active_selected_profiles ?? 0)")
                        Text("Cartões liberados: \(status.active_allowed_cards ?? 0)")
                    }

                    if let lastStatus = status.last_status, lastStatus != "idle" {
                        Text("Último status: \(lastStatus)")
                    }

                    let schedules = status.schedules ?? []
                    if !schedules.isEmpty {
                        Divider().padding(.vertical, 4)
                        Text("Próximos agendamentos")
                            .font(.subheadline)
                            .bold()
                        ForEach(schedules) { schedule in
                            Text("ID \(schedule.id): \(formatUnix(schedule.start_unix)) · \(schedule.profile_count) perfil(is) · \(recurrenceLabel(schedule.recurrence))")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                } else {
                    Text("Status indisponível.")
                        .foregroundColor(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private var scheduleBox: some View {
        GroupBox("Agendar modo reunião") {
            VStack(spacing: 12) {
                TextField("Atraso em minutos", text: $delayMinutes)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)

                TextField("Índices dos perfis", text: $profileIndices)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numbersAndPunctuation)

                Picker("Recorrência", selection: $recurrence) {
                    Text("Única").tag("none")
                    Text("Diária").tag("daily")
                    Text("Semanal").tag("weekly")
                }
                .pickerStyle(.segmented)

                if recurrence == "weekly" {
                    TextField("Dias 0-6", text: $weekdays)
                        .textFieldStyle(.roundedBorder)
                        .keyboardType(.numbersAndPunctuation)
                }

                Button("Agendar reunião") {
                    scheduleMeeting()
                }
                .buttonStyle(.borderedProminent)
                .disabled(loading)
                .frame(maxWidth: .infinity)
            }
        }
    }

    private var cancelBox: some View {
        GroupBox("Cancelar agendamento") {
            VStack(spacing: 12) {
                TextField("ID opcional", text: $cancelId)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(.numberPad)

                Button(cancelId.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Cancelar todos" : "Cancelar ID") {
                    cancelMeeting()
                }
                .buttonStyle(.bordered)
                .disabled(loading)
                .frame(maxWidth: .infinity)
            }
        }
    }

    private func endpoint(_ path: String) -> URL? {
        let root = baseURL.hasSuffix("/") ? String(baseURL.dropLast()) : baseURL
        return URL(string: "\(root)\(path)")
    }

    private func makeRequest(path: String, method: String, body: Data? = nil, completion: @escaping (Result<URLRequest, Error>) -> Void) {
        guard let url = endpoint(path) else {
            completion(.failure(AppRamoDoorError.message("URL inválida.")))
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        if let body = body {
            request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
            request.httpBody = body
        }
        if !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            request.setValue(apiKey, forHTTPHeaderField: "X-API-KEY")
            completion(.success(request))
            return
        }

        guard let user = Auth.auth().currentUser else {
            completion(.failure(AppRamoDoorError.message("Faça login para controlar a sala.")))
            return
        }

        user.getIDToken { token, error in
            if let error = error {
                completion(.failure(error))
                return
            }
            guard let token, !token.isEmpty else {
                completion(.failure(AppRamoDoorError.message("Não foi possível obter o token de autenticação.")))
                return
            }
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
            completion(.success(request))
        }
    }

    private func decodeResponse<T: Decodable>(_ type: T.Type, data: Data?, response: URLResponse?, error: Error?, completion: @escaping (Result<T, Error>) -> Void) {
        if let error = error {
            completion(.failure(error))
            return
        }

        guard let data = data else {
            completion(.failure(AppRamoDoorError.message("Sem dados")))
            return
        }

        if let httpResponse = response as? HTTPURLResponse, !(200...299).contains(httpResponse.statusCode) {
            if let apiError = try? JSONDecoder().decode(DoorAPIResponse.self, from: data), let error = apiError.error {
                completion(.failure(AppRamoDoorError.message(error)))
            } else {
                completion(.failure(AppRamoDoorError.message("HTTP \(httpResponse.statusCode)")))
            }
            return
        }

        do {
            completion(.success(try JSONDecoder().decode(type, from: data)))
        } catch {
            completion(.failure(error))
        }
    }

    private func openDoor() {
        loading = true
        errorMessage = nil
        successMessage = nil

        makeRequest(path: "/api/door/open", method: "POST") { requestResult in
            switch requestResult {
            case .failure(let error):
                DispatchQueue.main.async {
                    loading = false
                    errorMessage = error.localizedDescription
                }
            case .success(let request):
                session.dataTask(with: request) { data, response, error in
                    decodeResponse(DoorAPIResponse.self, data: data, response: response, error: error) { result in
                        DispatchQueue.main.async {
                            loading = false
                            switch result {
                            case .success(let payload):
                                if payload.ok {
                                    successMessage = payload.message ?? "Comando de abertura enviado."
                                } else {
                                    errorMessage = payload.error ?? "Resposta inesperada"
                                }
                            case .failure(let error):
                                errorMessage = error.localizedDescription
                            }
                        }
                    }
                }.resume()
            }
        }
    }

    private func refreshMeetingStatus() {
        loading = true
        errorMessage = nil

        makeRequest(path: "/api/meeting/status", method: "GET") { requestResult in
            switch requestResult {
            case .failure(let error):
                DispatchQueue.main.async {
                    loading = false
                    errorMessage = error.localizedDescription
                }
            case .success(let request):
                session.dataTask(with: request) { data, response, error in
                    decodeResponse(MeetingStatusResponse.self, data: data, response: response, error: error) { result in
                        DispatchQueue.main.async {
                            loading = false
                            switch result {
                            case .success(let status):
                                if status.ok {
                                    meetingStatus = status
                                } else {
                                    errorMessage = status.error ?? "Resposta inesperada"
                                }
                            case .failure(let error):
                                errorMessage = error.localizedDescription
                            }
                        }
                    }
                }.resume()
            }
        }
    }

    private func scheduleMeeting() {
        do {
            guard let minutes = Int64(delayMinutes.trimmingCharacters(in: .whitespacesAndNewlines)),
                  minutes > 0,
                  minutes <= 1440 else {
                throw AppRamoDoorError.message("O atraso deve ficar entre 1 e 1440 minutos.")
            }

            let payload = MeetingSchedulePayload(
                delay_seconds: minutes * 60,
                profile_indices: try parseProfileIndices(),
                recurrence: recurrence,
                weekdays: recurrence == "weekly" ? try parseWeekdays() : nil
            )
            let body = try JSONEncoder().encode(payload)
            loading = true
            errorMessage = nil
            successMessage = nil
            makeRequest(path: "/api/meeting/schedule", method: "POST", body: body) { requestResult in
                switch requestResult {
                case .failure(let error):
                    DispatchQueue.main.async {
                        loading = false
                        errorMessage = error.localizedDescription
                    }
                case .success(let request):
                    session.dataTask(with: request) { data, response, error in
                        decodeResponse(MeetingScheduleResponse.self, data: data, response: response, error: error) { result in
                            DispatchQueue.main.async {
                                loading = false
                                switch result {
                                case .success(let response):
                                    if response.ok {
                                        successMessage = "Reunião agendada. ID \(response.id ?? 0)."
                                        refreshMeetingStatus()
                                    } else {
                                        errorMessage = response.error ?? "Resposta inesperada"
                                    }
                                case .failure(let error):
                                    errorMessage = error.localizedDescription
                                }
                            }
                        }
                    }.resume()
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func cancelMeeting() {
        do {
            let trimmedId = cancelId.trimmingCharacters(in: .whitespacesAndNewlines)
            let body: Data?

            if trimmedId.isEmpty {
                body = nil
            } else if let id = Int64(trimmedId), id > 0 {
                body = try JSONEncoder().encode(MeetingCancelPayload(id: id))
            } else {
                throw AppRamoDoorError.message("ID de cancelamento inválido.")
            }

            loading = true
            errorMessage = nil
            successMessage = nil
            makeRequest(path: "/api/meeting/cancel", method: "POST", body: body) { requestResult in
                switch requestResult {
                case .failure(let error):
                    DispatchQueue.main.async {
                        loading = false
                        errorMessage = error.localizedDescription
                    }
                case .success(let request):
                    session.dataTask(with: request) { data, response, error in
                        decodeResponse(MeetingCancelResponse.self, data: data, response: response, error: error) { result in
                            DispatchQueue.main.async {
                                loading = false
                                switch result {
                                case .success(let response):
                                    if response.ok {
                                        successMessage = "\(response.canceled_count ?? 0) agendamento(s) cancelado(s)."
                                        cancelId = ""
                                        refreshMeetingStatus()
                                    } else {
                                        errorMessage = response.error ?? "Resposta inesperada"
                                    }
                                case .failure(let error):
                                    errorMessage = error.localizedDescription
                                }
                            }
                        }
                    }.resume()
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func parseProfileIndices() throws -> [Int] {
        let tokens = profileIndices
            .split { character in
                isListSeparator(character)
            }
            .map(String.init)

        if tokens.isEmpty {
            throw AppRamoDoorError.message("Informe ao menos um índice de perfil.")
        }

        return try tokens.map { token in
            guard let value = Int(token), value >= 0 else {
                throw AppRamoDoorError.message("Use apenas índices de perfil válidos.")
            }
            return value
        }
    }

    private func parseWeekdays() throws -> [Int]? {
        let tokens = weekdays
            .split { character in
                isListSeparator(character)
            }
            .map(String.init)

        if tokens.isEmpty {
            return nil
        }

        return try tokens.map { token in
            guard let value = Int(token), (0...6).contains(value) else {
                throw AppRamoDoorError.message("Dias da semana devem ficar entre 0 e 6.")
            }
            return value
        }
    }

    private func formatUnix(_ unix: Int64) -> String {
        guard unix > 0 else { return "-" }
        return Date(timeIntervalSince1970: TimeInterval(unix)).formatted(date: .abbreviated, time: .shortened)
    }

    private func recurrenceLabel(_ value: String) -> String {
        switch value {
        case "daily":
            return "diária"
        case "weekly":
            return "semanal"
        default:
            return "única"
        }
    }

    private func yesNo(_ value: Bool?) -> String {
        return (value ?? false) ? "sim" : "não"
    }

    private func isListSeparator(_ character: Character) -> Bool {
        return character == ","
            || character == ";"
            || character == " "
            || character == "\n"
            || character == "\t"
            || character == "\r"
    }
}

private enum AppRamoDoorError: LocalizedError {
    case message(String)

    var errorDescription: String? {
        switch self {
        case .message(let message):
            return message
        }
    }
}
