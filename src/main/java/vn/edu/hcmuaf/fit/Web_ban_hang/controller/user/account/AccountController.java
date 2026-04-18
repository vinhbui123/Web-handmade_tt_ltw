package vn.edu.hcmuaf.fit.Web_ban_hang.controller.user.account;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.hcmuaf.fit.Web_ban_hang.services.UserService;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@WebServlet(name = "AccountController", urlPatterns = "/account")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 3, // 3MB
        maxRequestSize = 1024 * 1024 * 10
)
public class AccountController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    private UserService userService;

    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("login");
            return;
        }

        request.setCharacterEncoding("UTF-8");

        // Get Inputs
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email"); // Handle null if disabled in JSP!
        String phoneNumber = request.getParameter("phoneNumber");

        String[] parts = fullName.trim().split(" ", 2);
        String firstName = parts.length > 0 ? parts[0] : "";
        String lastName = parts.length > 1 ? parts[1] : "";

        // Validate
        String errorMsg = userService.validateUpdateProfile(firstName, lastName, phoneNumber);
        if (errorMsg != null) {
            handleUpdateUserError(request, response, errorMsg);
            return;
        }

        // Update User Object Basic Info
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (email != null && !email.trim().isEmpty()) user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setAddress(request.getParameter("address"));
        user.setBio(request.getParameter("bio"));

        Part avatarPart = request.getPart("avatarUpload");

        if (avatarPart != null && avatarPart.getSize() > 0) {

            log.info("begin upload avatar");
            if (user.getAvatar() != null && !user.getAvatar().trim().isEmpty()) {
                String oldFilePath = request.getServletContext().getRealPath("/") + user.getAvatar();
                File oldFile = new File(oldFilePath);

                // If the file exists on server, delete it
                if (oldFile.exists() && oldFile.isFile()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        log.warn("Cant delete avatar file: {}", oldFilePath);
                    }
                }
            }

            String originalFileName = Paths.get(avatarPart.getSubmittedFileName()).getFileName().toString();
            // Make file save dont collision when upload
            String newFileName = System.currentTimeMillis() + "_" + originalFileName;

            String uploadPath = request.getServletContext().getRealPath("/images/avatars");
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String filePath = Paths.get(uploadPath, newFileName).toString();
            avatarPart.write(filePath);

            // Update the user object with the new database path
            user.setAvatar("images/avatars/" + newFileName);
        }

        // Update Database
        boolean success = this.userService.updateUser(user);

        if (!success) {
            request.setAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật thông tin người dùng.");
            request.getRequestDispatcher("account.jsp").forward(request, response);
            return;
        }

        // Update Session
        session.setAttribute("user", user);
        request.setAttribute("successMessage", "Thông tin của bạn đã được cập nhật thành công.");
        request.getRequestDispatcher("account.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("account.jsp").forward(request, response);
    }

    private void handleUpdateUserError(HttpServletRequest request, HttpServletResponse response, String error) throws ServletException, IOException {
        request.setAttribute("errorMessage", error);
        User tempUser = new User();

        String fullName = request.getParameter("fullName");
        String[] parts = (fullName != null) ? fullName.trim().split(" ", 2) : new String[]{"", ""};
        String firstName = parts.length > 0 ? parts[0] : "";
        String lastName = parts.length > 1 ? parts[1] : "";

        tempUser.setFirstName(firstName);
        tempUser.setLastName(lastName);
        tempUser.setEmail(request.getParameter("email"));
        tempUser.setPhoneNumber(request.getParameter("phoneNumber"));
        tempUser.setAddress(request.getParameter("address"));
        tempUser.setBio(request.getParameter("bio"));

        HttpSession session = request.getSession();
        User realUser = (User) session.getAttribute("user");
        if (realUser != null) {
            tempUser.setAvatar(realUser.getAvatar());
            tempUser.setUsername(realUser.getUsername());
            if (tempUser.getEmail() == null) tempUser.setEmail(realUser.getEmail());
        }
        request.setAttribute("user", tempUser);
        request.getRequestDispatcher("account.jsp").forward(request, response);
    }

}