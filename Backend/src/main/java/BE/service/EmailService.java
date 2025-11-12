package BE.service;

import BE.entity.*;
import BE.model.EmailDetail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;



@Service
public class EmailService {

    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    JavaMailSender mailSender;


    public void sendMailTemplate(EmailDetail emailDetail){
        try {
            Context context = new Context();
            context.setVariable("name", emailDetail.getFullName());
//            context.setVariable("button", emailDetail.getButtonValue());
//            context.setVariable("link", emailDetail.getLink());

            String text = templateEngine.process("email-template", context);

            // creating a simple mail message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);


            // setting up necessary details
            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getRecipient());
            mimeMessageHelper.setText(text , true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());
            mailSender.send(mimeMessage);


        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendPasswordResetEmail(EmailDetail emailDetail) {
        try {
            Context context = new Context();
            context.setVariable("name", emailDetail.getFullName());
            context.setVariable("resetLink", emailDetail.getLink());

            String text = templateEngine.process("forgot-password-template", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetail.getRecipient());
            mimeMessageHelper.setText(text , true);
            mimeMessageHelper.setSubject(emailDetail.getSubject());

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage(), e);
        }
    }

    public void sendInvoiceEmail(Invoice invoice) {
        try {
            // Lấy các đối tượng liên quan từ hóa đơn
            Maintenance maintenance = invoice.getMaintenance();
            if (maintenance == null) throw new RuntimeException("Không tìm thấy Maintenance cho Invoice ID: " + invoice.getInvoiceID());

            Orders order = maintenance.getOrders();
            if (order == null) throw new RuntimeException("Không tìm thấy Order cho Maintenance ID: " + maintenance.getMaintenanceID());

            Customer customer = order.getCustomer();
            if (customer == null) throw new RuntimeException("Không tìm thấy Customer cho Order ID: " + order.getOrderID());

            ServiceCenter center = order.getServiceCenter();
            if (center == null) throw new RuntimeException("Không tìm thấy ServiceCenter cho Order ID: " + order.getOrderID());

            // Tạo Thymeleaf Context
            Context context = new Context();

            // Service Center
            context.setVariable("centerName", center.getName());
            context.setVariable("centerAddress", center.getAddress());
            context.setVariable("centerPhone", center.getPhone());
            context.setVariable("centerEmail", center.getEmail());

            // Customer
            context.setVariable("customerName", customer.getName());
            context.setVariable("customerEmail", customer.getEmail());
            context.setVariable("customerPhone", customer.getPhone());
            context.setVariable("customerAddress", customer.getAddress()); //

            // Invoice
            context.setVariable("invoiceId", String.format("#%06d", invoice.getInvoiceID())); // Định dạng cho đẹp
            context.setVariable("issuedDate", invoice.getIssuedDate());
            context.setVariable("totalAmount", invoice.getTotalAmount());
            context.setVariable("invoiceDetails", invoice.getInvoiceDetails()); //

            // Xử lý template mới
            String text = templateEngine.process("invoice-email-template", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setFrom("hieuthong15082005@gmail.com");
            mimeMessageHelper.setTo(customer.getEmail());
            mimeMessageHelper.setText(text , true);
            mimeMessageHelper.setSubject("Hóa đơn thanh toán dịch vụ " + String.format("#%06d", invoice.getInvoiceID()));

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Gửi email hóa đơn thất bại: " + e.getMessage(), e);
        }
    }
}
