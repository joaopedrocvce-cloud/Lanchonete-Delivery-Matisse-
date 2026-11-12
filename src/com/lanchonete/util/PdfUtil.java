package com.lanchonete.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lanchonete.model.ItemPedido;
import com.lanchonete.model.Pedido;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Classe utilitaria responsavel por gerar o comprovante do pedido em PDF,
 * utilizando a biblioteca iText (funcionalidade extra do trabalho).
 */
public class PdfUtil {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Gera o arquivo PDF do comprovante do pedido no caminho informado.
     *
     * @param pedido        pedido (com itens ja carregados) a ser impresso
     * @param caminhoArquivo caminho completo do arquivo .pdf a ser gerado
     */
    public static void gerarComprovante(Pedido pedido, String caminhoArquivo)
            throws DocumentException, IOException {

        Document documento = new Document();
        try (FileOutputStream saida = new FileOutputStream(caminhoArquivo)) {
            PdfWriter.getInstance(documento, saida);
            documento.open();

            Font fonteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
            Font fonteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.GRAY);
            Font fonteNormal = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font fonteNegrito = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font fonteCabecalhoTabela = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);

            Paragraph titulo = new Paragraph("Comprovante de Pedido - Lanchonete Delivery", fonteTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph subtitulo = new Paragraph("Pedido N. " + pedido.getId(), fonteSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            subtitulo.setSpacingAfter(20);
            documento.add(subtitulo);

            documento.add(new Paragraph("Cliente: " + pedido.getCliente().getNome(), fonteNormal));
            documento.add(new Paragraph("Telefone: " + nvl(pedido.getCliente().getTelefone()), fonteNormal));
            documento.add(new Paragraph("Endereco de entrega: " + nvl(pedido.getCliente().getEndereco()), fonteNormal));
            documento.add(new Paragraph("Data/Hora do pedido: " + pedido.getDataHora().format(FORMATO_DATA), fonteNormal));
            documento.add(new Paragraph("Forma de pagamento: " + nvl(pedido.getFormaPagamento()), fonteNormal));
            documento.add(new Paragraph("Status: " + pedido.getStatus(), fonteNormal));

            Paragraph espaco = new Paragraph(" ");
            documento.add(espaco);

            PdfPTable tabela = new PdfPTable(new float[]{4f, 1f, 2f, 2f});
            tabela.setWidthPercentage(100);

            adicionarCelulaCabecalho(tabela, "Produto", fonteCabecalhoTabela);
            adicionarCelulaCabecalho(tabela, "Qtd.", fonteCabecalhoTabela);
            adicionarCelulaCabecalho(tabela, "Valor Unit.", fonteCabecalhoTabela);
            adicionarCelulaCabecalho(tabela, "Subtotal", fonteCabecalhoTabela);

            for (ItemPedido item : pedido.getItens()) {
                tabela.addCell(new PdfPCell(new Paragraph(item.getProduto().getNome(), fonteNormal)));
                PdfPCell celQtd = new PdfPCell(new Paragraph(String.valueOf(item.getQuantidade()), fonteNormal));
                celQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
                tabela.addCell(celQtd);
                PdfPCell celValor = new PdfPCell(new Paragraph("R$ " + item.getValorUnitario(), fonteNormal));
                celValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(celValor);
                PdfPCell celSub = new PdfPCell(new Paragraph("R$ " + item.getSubtotal(), fonteNormal));
                celSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabela.addCell(celSub);
            }

            documento.add(tabela);

            Paragraph total = new Paragraph("Valor Total: R$ " + pedido.getValorTotal(), fonteNegrito);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(15);
            documento.add(total);

            Paragraph rodape = new Paragraph("Obrigado pela preferencia!", fonteSubtitulo);
            rodape.setAlignment(Element.ALIGN_CENTER);
            rodape.setSpacingBefore(30);
            documento.add(rodape);

            documento.close();
        }
    }

    private static void adicionarCelulaCabecalho(PdfPTable tabela, String texto, Font fonte) {
        PdfPCell celula = new PdfPCell(new Paragraph(texto, fonte));
        celula.setBackgroundColor(new BaseColor(230, 126, 34));
        celula.setHorizontalAlignment(Element.ALIGN_CENTER);
        celula.setPadding(6);
        tabela.addCell(celula);
    }

    private static String nvl(String texto) {
        return (texto == null || texto.trim().isEmpty()) ? "-" : texto;
    }
}
