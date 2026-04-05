import java.sql.*;

public class UserLogin {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://localhost:3306/word_app?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "Hujihan608624"; 

    public static void main(String[] args) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);
            // 打开连接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);
            
            // 执行查询
            System.out.println("实例化 PreparedStatement...");
            String sql = "SELECT * FROM users WHERE username=? AND password_hash=?";
            stmt = conn.prepareStatement(sql);

            // 测试数据
            String username = "admin";
            String password = "password";
            
            // 设置参数
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            // 执行查询
            ResultSet rs = stmt.executeQuery();

            // 处理结果集
            if(rs.next()){
                System.out.println("登录成功！");
            }else{
                System.out.println("登录失败！");
            }

            // 关闭资源
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
             se.printStackTrace();
        }catch(Exception e){
             e.printStackTrace();
        }finally{
             try{if(stmt!=null)stmt.close();}catch(SQLException se2){}
             try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}
        }
        System.out.println("Goodbye!");
    }
}


            
//             String sql = "SELECT * FROM users WHERE username=? AND password_hash=?";
//             ResultSet rs = stmt.executeQuery(sql);
        
//             while(rs.next()){
//                 int id  = rs.getInt("id");
//                 String name = rs.getString("name");
//                 String url = rs.getString("url");
    
//                 System.out.print("ID: " + id);
//                 System.out.print(", 站点名称: " + name);
//                 System.out.print(", 站点 URL: " + url);
//                 System.out.print("\n");
//             }
//             rs.close();
//             stmt.close();
//             conn.close();
//         }catch(SQLException se){
//             se.printStackTrace();
//         }catch(Exception e){
//             e.printStackTrace();
//         }finally{
//             try{if(stmt!=null)stmt.close();}catch(SQLException se2){}
//             try{if(conn!=null)conn.close();}catch(SQLException se){se.printStackTrace();}
//         }
//         System.out.println("Goodbye!");
//     }
// }