/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trinm.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import trinm.tblcar.TblCarDAO;
import trinm.tblcar.TblCarDTO;

/**
 *
 * @author tring
 */
@WebServlet(name = "CarLoadServlet", urlPatterns = {"/CarLoadServlet"})
public class CarLoadServlet extends HttpServlet {

    private final Logger LOGGER = Logger.getLogger(this.getClass());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out = null;
        try {
            response.setContentType("text/html;charset=UTF-8");
            out = response.getWriter();
            String url = "studentPage";
            List<TblCarDTO> carList = new ArrayList<>();
            int num = 0;
            try {
                HttpSession session = request.getSession();
                TblCarDAO dao = new TblCarDAO();
                dao.loadCar(1);
                carList = dao.getCarLoad();
                num = dao.getNum();
                dao.loadCategory();
                List<String> category = dao.getCategory();
                category.add(0, "All");
                session.setAttribute("CATEGORYLIST", category);
                session.setAttribute("CARLIST", carList);
                session.setAttribute("NUM", num);

            } finally {
                response.sendRedirect(url);
            }
        } catch (IOException ex) {
            BasicConfigurator.configure();
            LOGGER.error("CarLoadServlet_IO: " + ex.getMessage());
        } catch (SQLException ex) {
            BasicConfigurator.configure();
            LOGGER.error("CarLoadServlet_SQL: " + ex.getMessage());
        } catch (NamingException ex) {
            BasicConfigurator.configure();
            LOGGER.error("LoadSubjectServlet_Naming: " + ex.getMessage());
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
