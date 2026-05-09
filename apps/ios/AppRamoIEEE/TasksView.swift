import SwiftUI
import FirebaseFirestore

struct TasksView: View {
    @EnvironmentObject var viewModel: AppViewModel
    @State private var showAddDialog = false
    
    var body: some View {
        List {
            if viewModel.tasks.isEmpty {
                Text("Sem tarefas agendadas.")
                    .foregroundColor(.gray)
                    .padding()
            } else {
                ForEach(viewModel.tasks) { task in
                    HStack(spacing: 12) {
                        // BOTÃO DE CHECKBOX (Mais confiável que Image + TapGesture)
                        Button(action: {
                            toggleTask(task)
                        }) {
                            Image(systemName: (task.completed ?? false) ? "checkmark.square.fill" : "square")
                                .resizable()
                                .frame(width: 24, height: 24)
                                .foregroundColor((task.completed ?? false) ? .green : .gray)
                        }
                        .buttonStyle(PlainButtonStyle()) // Impede que o clique selecione a linha toda
                        
                        VStack(alignment: .leading) {
                            Text(task.title)
                                .font(.headline)
                                .strikethrough(task.completed ?? false)
                                .foregroundColor((task.completed ?? false) ? .gray : .primary)
                            
                            if !task.description.isEmpty {
                                Text(task.description)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Text(task.chapter)
                                .font(.caption2)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(Color.gray.opacity(0.2))
                                .cornerRadius(4)
                        }
                    }
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            deleteTask(task)
                        } label: {
                            Label("Apagar", systemImage: "trash")
                        }
                    }
                }
            }
        }
        .navigationTitle("Tarefas")
        .toolbar {
            Button(action: { showAddDialog = true }) {
                Image(systemName: "plus")
            }
        }
        .sheet(isPresented: $showAddDialog) {
            let userChapters = viewModel.currentUser?.chapterRoles.keys.sorted() ?? []
            AddTaskView(chapters: userChapters)
        }
    }
    
    func toggleTask(_ task: ChapterTask) {
        guard let id = task.id else { return }
        // Valor atual (se for nulo, assume false)
        let currentStatus = task.completed ?? false
        
        // Atualiza no banco (O listener do AppViewModel vai atualizar a tela automaticamente)
        Firestore.firestore().collection("tasks").document(id).updateData(["completed": !currentStatus])
    }
    
    func deleteTask(_ task: ChapterTask) {
        guard let id = task.id else { return }
        Firestore.firestore().collection("tasks").document(id).delete()
    }
}

struct AddTaskView: View {
    @Environment(\.dismiss) var dismiss
    let chapters: [String]
    
    @State private var title = ""
    @State private var description = ""
    @State private var selectedChapter = ""
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Detalhes")) {
                    TextField("Título", text: $title)
                    TextField("Descrição", text: $description)
                }
                
                Section(header: Text("Capítulo")) {
                    if chapters.isEmpty {
                        Text("Geral")
                    } else {
                        Picker("Selecione", selection: $selectedChapter) {
                            ForEach(chapters, id: \.self) { chapter in
                                Text(chapter).tag(chapter)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Nova Tarefa")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancelar") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Salvar") {
                        saveTask()
                    }
                    .disabled(title.isEmpty)
                }
            }
            .onAppear {
                if let first = chapters.first {
                    selectedChapter = first
                }
            }
        }
    }
    
    func saveTask() {
        let chapterToSave = selectedChapter.isEmpty ? (chapters.first ?? "Geral") : selectedChapter
        
        let newTask: [String: Any] = [
            "title": title,
            "description": description,
            "chapter": chapterToSave,
            "completed": false
        ]
        
        Firestore.firestore().collection("tasks").addDocument(data: newTask)
        dismiss()
    }
}
