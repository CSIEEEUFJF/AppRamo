import SwiftUI
import FirebaseFirestore
import EventKit

// Singleton para gerenciar o EventStore e evitar erros de conexão (XPC)
class CalendarManager {
    static let shared = CalendarManager()
    let eventStore = EKEventStore()
}

struct CalendarView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var showAddEvent = false
    private var canManageContent: Bool {
        AccessPolicy.canManageContent(viewModel.currentUser?.chapterRoles ?? [:])
    }
    
    var body: some View {
        List(viewModel.events) { event in
            VStack(alignment: .leading) {
                Text(event.title)
                    .font(.headline)
                
                if !event.description.isEmpty {
                    Text(event.description)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
                
                // Exibe data e hora
                if let start = event.startTime {
                    HStack {
                        Image(systemName: "clock")
                        Text(start.formatted(date: .abbreviated, time: .shortened))
                    }
                    .font(.caption2)
                    .padding(.top, 2)
                    .foregroundColor(.blue)
                }
                
                // Exibe Local
                if !event.location.isEmpty {
                    Text(event.location)
                        .font(.caption2)
                        .italic()
                        .padding(.top, 1)
                }
                
                // Exibe Capítulo (Badge)
                Text(event.chapter)
                    .font(.caption2)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(4)
                    .padding(.top, 4)
            }
            .swipeActions(edge: .trailing) {
                if canManageContent {
                    Button(role: .destructive) {
                        deleteEvent(event)
                    } label: {
                        Label("Apagar", systemImage: "trash")
                    }
                }
            }
        }
        .navigationTitle("Agenda")
        .toolbar {
            if canManageContent {
                Button {
                    showAddEvent = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showAddEvent) {
            let availableChapters = AccessPolicy.visibleChapters(viewModel.currentUser?.chapterRoles ?? [:])
            AddEventView(availableChapters: availableChapters)
        }
    }
    
    func deleteEvent(_ event: ChapterEvent) {
        guard let id = event.id else { return }
        Firestore.firestore().collection("events").document(id).delete()
    }
}

struct AddEventView: View {
    @Environment(\.dismiss) var dismiss
    
    // Recebe a lista de capítulos
    let availableChapters: [String]
    
    @State private var title = ""
    @State private var description = ""
    @State private var location = ""
    @State private var date = Date()
    @State private var endDate = Date().addingTimeInterval(3600)
    @State private var selectedChapter = AccessPolicy.globalChapter
    @State private var addToCalendar = true
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Detalhes do Evento")) {
                    TextField("Título", text: $title)
                    TextField("Descrição", text: $description)
                    TextField("Local", text: $location)
                }
                
                Section(header: Text("Público Alvo")) {
                    // SELETOR DE CAPÍTULO
                    Picker("Capítulo", selection: $selectedChapter) {
                        ForEach(availableChapters, id: \.self) { chapter in
                            Text(chapter).tag(chapter)
                        }
                    }
                    .pickerStyle(.menu)
                }
                
                Section(header: Text("Data e Hora")) {
                    DatePicker("Início", selection: $date)
                        .onChange(of: date) { newDate in
                            if endDate <= newDate {
                                endDate = newDate.addingTimeInterval(3600)
                            }
                        }
                    DatePicker("Fim", selection: $endDate, in: date...)
                }
                
                Section {
                    Toggle("Salvar na Agenda do iPhone", isOn: $addToCalendar)
                }
            }
            .navigationTitle("Novo Evento")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salvar") {
                        saveEvent()
                    }
                    .disabled(title.isEmpty)
                }
            }
        }
    }
    
    func saveEvent() {
        let newEvent: [String: Any] = [
            "title": title,
            "description": description,
            "location": location,
            "startTime": date,
            "endTime": endDate,
            "chapter": selectedChapter // Usa a seleção do Picker
        ]
        
        // 1. Salva no Firebase
        Firestore.firestore().collection("events").addDocument(data: newEvent) { error in
            if error == nil && addToCalendar {
                // 2. Salva no Calendário Nativo
                saveToDeviceCalendar()
            }
            dismiss()
        }
    }
    
    func saveToDeviceCalendar() {
        let store = CalendarManager.shared.eventStore
        
        if #available(iOS 17.0, *) {
            store.requestWriteOnlyAccessToEvents { granted, error in
                if granted { createEKEvent(store: store) }
            }
        } else {
            store.requestAccess(to: .event) { granted, error in
                if granted { createEKEvent(store: store) }
            }
        }
    }
    
    func createEKEvent(store: EKEventStore) {
        let event = EKEvent(eventStore: store)
        event.title = "[\(selectedChapter)] \(title)" // Adiciona tag ao título no calendário
        event.startDate = date
        event.endDate = endDate
        event.notes = description
        event.location = location
        event.calendar = store.defaultCalendarForNewEvents
        
        do {
            try store.save(event, span: .thisEvent)
            print("✅ Evento salvo no calendário!")
        } catch {
            print("❌ Erro ao salvar: \(error)")
        }
    }
}
