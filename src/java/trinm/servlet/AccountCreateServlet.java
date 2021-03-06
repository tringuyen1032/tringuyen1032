/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trinm.servlet;

import com.restfb.types.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import trinm.facebook.RestFacebook;
import trinm.tbluser.TblUserCreateError;
import trinm.tbluser.SendEmail;
import trinm.tbluser.TblUserDAO;
import trinm.tbluser.TblUserHash;

/**
 *
 * @author tring
 */
@WebServlet(name = "AccountCreateServlet", urlPatterns = {"/AccountCreateServlet"})
public class AccountCreateServlet extends HttpServlet {

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
            String url = "createNewAccountJSP";
            String codeFB = request.getParameter("code");
            HttpSession session = request.getSession();
            try {
                if (codeFB == null || codeFB.isEmpty()) {
                    String username = request.getParameter("txtUsername");
                    if (username == null) {
                        username = "";
                    }
                    String password = request.getParameter("txtPassword");
                    if (password == null) {
                        password = "";
                    }
                    String confirmation = request.getParameter("txtConfirm");
                    if (confirmation == null) {
                        confirmation = "";
                    }
                    String fullname = request.getParameter("txtFullname");
                    if (fullname == null) {
                        fullname = "";
                    }
                    String phone = request.getParameter("txtPhone");
                    long number = 0;
                    if (phone == null) {
                        phone = "";
                    }
                    String address = request.getParameter("txtAddress");
                    if (address == null) {
                        address = "";
                    }
                    TblUserCreateError errors = new TblUserCreateError();
                    //1.    Check validate of 4 users
                    boolean errorFound = false;
                    if (!isValidMail(username)) {
                        errorFound = true;
                        errors.setUsernameLengthErr("Email requires typing with format a@gmail.com");
                    }
                    if (password.trim().length() < 6 || password.trim().length() > 20) {
                        errorFound = true;
                        errors.setPasswordLengthErr("Password requires typing form 6 to 20 chars");
                    } else if (!confirmation.trim().equals(password.trim())) {
                        errorFound = true;
                        errors.setConfirmationNotMatched("Confirm must match password");
                    }
                    if (fullname.trim().length() < 6 || fullname.trim().length() > 50) {
                        errorFound = true;
                        errors.setFullnameLengthErr("Full name requires typing form 6 to 50 chars");
                    }
                    if (phone.trim().length() < 1) {
                        errorFound = true;
                        errors.setPhoneLengthErr("Please input phone number");
                    } else {
                        try {
                            number = Long.parseLong(phone);
                        } catch (NumberFormatException e) {
                            errorFound = true;
                            errors.setPhoneTypeErr("Please input number for phone number");
                        }
                    }
                    if (address.trim().length() < 1) {
                        errorFound = true;
                        errors.setAddressLengthErr("Please input address");
                    }
                    if (errorFound) {
                        //2.1    stores into Scope
                        session.setAttribute("CREATEER", errors);
                        session.setAttribute("USERNAMESIGNUP", username);
                        session.setAttribute("FULLNAMESIGNUP", fullname);
                        session.setAttribute("PHONESIGNUP", phone);
                        session.setAttribute("ADDRESSSIGNUP", address);
                    } else {
                        TblUserDAO dao = new TblUserDAO();
                        password = new TblUserHash(password).toHexString();
                        dao.createAccount(username, password, fullname, false, "New", number, address);
                        SendEmail mailSend = new SendEmail();
                        String code = mailSend.sendEmail(username);
                        session.setAttribute("CREATEACCOUNT", username);
                        session.setAttribute("VERIFYCODE", code);
                        url = "verifyPage";
                    }
                    session.setAttribute("MSG", null);
                } else {
                    String accessToken = RestFacebook.getToken(codeFB, "http://localhost:8084/CarRental/createRecord");
                    User user = RestFacebook.getUserInfo(accessToken);
                    String username = user.getId();
                    String fullname = user.getName();
                    String address = user.getEmail();
                    TblUserDAO dao = new TblUserDAO();
                    url = "loginPage";
                    try {
                        dao.createAccount(username, "", fullname, false, "new", address);
                        SendEmail mailSend = new SendEmail();
                        String code = mailSend.sendEmail(address);
                        session.setAttribute("CREATEACCOUNT", username);
                        session.setAttribute("VERIFYCODE", code);
                        url = "verifyPage";
                    } catch (SQLException ex) {
                        if (ex.getMessage().contains("duplicate")) {
                            session.setAttribute("MSG", "This Facebook account has been registed ");
                        }
                        LOGGER.error("CreateRecordServlet_SQLException: " + ex.getMessage());
                    }
                }
            } catch (NullPointerException ex) {
                BasicConfigurator.configure();
                LOGGER.error("CreateRecordServlet_NullPointer: " + ex.getMessage());
            } catch (NamingException ex) {
                BasicConfigurator.configure();
                LOGGER.error("CreateRecordServlet_NamingException: " + ex.getMessage());
            } catch (SQLException ex) {
                BasicConfigurator.configure();
                LOGGER.error("CreateRecordServlet_SQLException: " + ex.getMessage());
            } finally {
                response.sendRedirect(url);
            }
        } catch (IOException ex) {
            BasicConfigurator.configure();
            LOGGER.error("CreateRecordServlet_IO: " + ex.getMessage());
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

    private boolean isValidMail(String email) throws NullPointerException {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

}
