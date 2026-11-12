package com.lanchonete.util;

import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Classe utilitaria responsavel por enviar o comprovante do pedido por
 * e-mail ao cliente (funcionalidade extra do trabalho), usando a API
 * JavaMail com um servidor SMTP (ex.: Gmail).
 *
 * IMPORTANTE: preencha EMAIL_REMETENTE e SENHA_REMETENTE com uma conta de
 * e-mail valida. Se usar Gmail, e necessario gerar uma "Senha de app" nas
 * configuracoes de seguranca da conta Google (a senha normal da conta nao
 * funciona com acesso de aplicativos externos).
 */
public class EmailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_REMETENTE = "";
    private static final String SENHA_REMETENTE = "";

    /**
     * Envia um e-mail com o comprovante do pedido em anexo.
     *
     * @param destinatario    e-mail do cliente
     * @param assunto         assunto do e-mail
     * @param corpo           corpo (texto) da mensagem
     * @param caminhoAnexoPdf caminho do arquivo PDF a ser anexado (pode ser null)
     */
    public static void enviarComprovante(String destinatario, String assunto, String corpo,
            String caminhoAnexoPdf) throws MessagingException {

        Properties propriedades = new Properties();
        propriedades.put("mail.smtp.auth", "true");
        propriedades.put("mail.smtp.starttls.enable", "true");
        propriedades.put("mail.smtp.host", SMTP_HOST);
        propriedades.put("mail.smtp.port", SMTP_PORT);

        Session sessao = Session.getInstance(propriedades, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_REMETENTE);
            }
        });

        MimeMessage mensagem = new MimeMessage(sessao);
        try {
            mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE, "Lanchonete Delivery"));
        } catch (java.io.UnsupportedEncodingException e) {
            try {
                mensagem.setFrom(new InternetAddress(EMAIL_REMETENTE));
            } catch (MessagingException ex) {
                throw ex;
            }
        }
        mensagem.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        mensagem.setSubject(assunto);

        MimeBodyPart parteTexto = new MimeBodyPart();
        parteTexto.setText(corpo);

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(parteTexto);

        if (caminhoAnexoPdf != null && !caminhoAnexoPdf.trim().isEmpty()) {
            File arquivo = new File(caminhoAnexoPdf);
            if (arquivo.exists()) {
                MimeBodyPart parteAnexo = new MimeBodyPart();
                FileDataSource fonteDados = new FileDataSource(arquivo);
                parteAnexo.setDataHandler(new DataHandler(fonteDados));
                parteAnexo.setFileName(arquivo.getName());
                multipart.addBodyPart(parteAnexo);
            }
        }

        mensagem.setContent(multipart);
        Transport.send(mensagem);
    }
}
