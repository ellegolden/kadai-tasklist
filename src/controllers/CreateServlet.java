package controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import models.Task;
import models.validators.TaskValidator;
import utils.DBUtil;

/**
 * Servlet implementation class CreateServlet
 */
@WebServlet("/create")
public class CreateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // セキュリティ処理(CSRF対策のチェック)
        // _token に値がセットされていなかったりセッションIDと値が異なったりしたら
        // データの登録ができない

        String _token = (String) request.getParameter("_token");
        if (_token != null && _token.equals(request.getSession().getId())) {

            // EntityManagerの利用を開始してデータをやりとりするための変数emを用意する
            EntityManager em = DBUtil.createEntityManager();

            // Taskクラスの変数mを用意して、各カラムのデータをセットする
            Task m = new Task();

            String content = request.getParameter("content");
            m.setContent(content);

            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            m.setCreated_at(currentTime);
            m.setUpdated_at(currentTime);

            // バリデーションを実行してエラーがあったら新規登録のフォームに戻る
            List<String> errors = TaskValidator.validate(m);
            if (errors.size() > 0) {
                em.close();

                // フォームに初期値を設定、さらにエラーメッセージを送る
                request.setAttribute("_token", request.getSession().getId());
                request.setAttribute("tasks", m);
                request.setAttribute("errors", errors);

                RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/views/properties/new.jsp");
                rd.forward(request, response);

            } else {

                em.getTransaction().begin(); // トランザクション処理の開始
                em.persist(m); // 処理内容をデータベースに保存
                em.getTransaction().commit(); // データベースの変更を確定
                request.getSession().setAttribute("flush", "登録が完了しました。");
                em.close(); // EntityManagerを終了する

                response.sendRedirect(request.getContextPath() + "/index"); // (/indexへリダイレクトする)
            }
        }
    }
}
