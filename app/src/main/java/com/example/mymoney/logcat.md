Trong ứng dụng MyMoney của bạn, các folder data và domain được tổ chức theo kiến trúc Clean Architecture. Đây là một cách phân lớp giúp mã nguồn dễ bảo trì, dễ kiểm thử và tách biệt rõ ràng giữa logic nghiệp vụ (business logic) và chi tiết kỹ thuật (database, network).
Dưới đây là giải thích chi tiết về luồng hoạt động của dữ liệu trong hai folder này:
1. Folder domain (Lớp Nghiệp Vụ)
   Đây là "trái tim" của ứng dụng, nơi chứa các quy tắc nghiệp vụ mà không phụ thuộc vào bất kỳ công nghệ hay thư viện nào (như Room hay Supabase).
   •
   model/: Chứa các Domain Models (ví dụ: TransactionModel, CategoryModel). Đây là các data class thuần túy dùng để hiển thị lên UI.
   •
   repository/: Chứa các Interfaces (giao diện). Nó định nghĩa các hành động có thể thực hiện (như getAllTransactions(), addTransaction()) nhưng không cài đặt cách thực hiện.
   •
   usecase/: Mỗi Use Case là một hành động nghiệp vụ duy nhất (ví dụ: GetTransactionsUseCase, AddTransactionUseCase). Nó đóng vai trò điều phối các Repository để thực hiện một công việc cụ thể.
2. Folder data (Lớp Dữ Liệu)
   Đây là nơi hiện thực hóa các yêu cầu từ lớp domain. Nó quyết định dữ liệu lấy từ đâu (Local DB hay Cloud).
   •
   local/: Xử lý dữ liệu dưới máy (Sử dụng Room Database).
   ◦
   entity/: Các class định nghĩa bảng trong database (ví dụ: TransactionEntity).
   ◦
   dao/: Các phương thức truy vấn SQL.
   •
   remote/: Xử lý dữ liệu trên server (Sử dụng Supabase).
   •
   repository/: Đây là nơi cài đặt (Implementation) các interface từ domain. Ví dụ, TransactionRepositoryImpl sẽ lấy dữ liệu từ TransactionDao, sau đó chuyển đổi nó thành TransactionModel để trả về cho domain.
3. Luồng hoạt động của dữ liệu (Data Flow)
   Để dễ hình dung, hãy xem luồng đi của một hành động "Lấy danh sách giao dịch":
   Bước 1: Request từ UI
   ViewModel gọi GetTransactionsUseCase.
   Bước 2: Use Case điều phối
   GetTransactionsUseCase gọi phương thức repository.getAllTransactions(userId). Lưu ý: Lúc này nó chỉ biết đến cái "tên" phương thức trong domain/repository.
   Bước 3: Thực thi tại Data Layer
   TransactionRepositoryImpl (trong app/src/main/java/com/example/mymoney/data/repository) sẽ nhận lệnh. Luồng dữ liệu bên trong như sau:
   Explore project structure
1.
Nó gọi TransactionDao để lấy danh sách TransactionEntity từ database Room.
I am starting to explore the project structure to understand the data and domain folders.
2.
Mapping (Chuyển đổi): Vì UI chỉ hiểu TransactionModel (domain), Repository sẽ dùng các hàm mapper (như toModel()) để biến TransactionEntity -> TransactionModel.
Files changed
3.
Nếu có yêu cầu đồng bộ, nó cũng có thể gọi SupabaseClient để đẩy hoặc kéo dữ liệu từ server.
Task List
Bước 4: Trả kết quả
Explain data flow in Vietnamese
Dữ liệu đã được chuyển đổi thành TransactionModel sẽ quay ngược lại qua UseCase -> ViewModel -> Hiển thị lên màn hình.
I have explored the project structure and understood the Clean Architecture pattern used. I am now preparing the explanation in Vietnamese.
4. Tại sao lại chia như vậy?
1.
Tính độc lập: Nếu sau này bạn không dùng Supabase nữa mà chuyển sang Firebase, bạn chỉ cần sửa đổi code trong folder app/src/main/java/com/example/mymoney/data/remote và app/src/main/java/com/example/mymoney/data/repository. Toàn bộ folder domain và UI (ViewModel, Screen) sẽ không cần thay đổi một dòng code nào.
2.
Dễ kiểm thử (Testing): Bạn có thể viết Unit Test cho UseCase bằng cách giả lập (mock) Repository mà không cần quan tâm đến database thật.
3.
Dữ liệu sạch: UI không bao giờ thấy được các class Entity của Database. Điều này ngăn chặn việc logic hiển thị bị dính chặt vào cấu trúc bảng của DB.
Tóm lại: domain nói cho ứng dụng biết cần làm gì, còn data thực hiện việc làm như thế nào. Dữ liệu đi qua data sẽ được "gọt giũa" (map) để trở thành các object sạch sẽ trước khi đưa vào domain.