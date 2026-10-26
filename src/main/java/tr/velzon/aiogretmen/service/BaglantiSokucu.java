package tr.velzon.aiogretmen.service;

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * AI ÇIKTISI markdown'ı için: bağlantı ve görselleri metne indirger (least-privilege). escapeHtml ham
 * HTML'i keser ama {@code [x](https://…)} / {@code ![x](https://…)} yine {@code <a>}/{@code <img>} olur —
 * dolaylı prompt injection'la çıktıya sızan bir URL veriyi tek tıkla (görselde tıklamasız) dışarı taşır
 * (EchoLeak sınıfı, content/kaynaklar/llm-uygulama-guvenligi.md §4d). Yalnız güvenilmeyen (AI) metne
 * uygulanır; content/ altındaki elle yazılmış müfredatın bağlantıları korunur.
 */
final class BaglantiSokucu implements PostProcessor {

    @Override
    public Node process(Node belge) {
        List<Node> hedefler = new ArrayList<>();
        belge.accept(new AbstractVisitor() {
            @Override
            public void visit(Link link) {
                hedefler.add(link);
                visitChildren(link);
            }

            @Override
            public void visit(Image gorsel) {
                hedefler.add(gorsel); // alt metni çocuklardadır; görselin içine inmeye gerek yok
            }
        });
        for (Node dugum : hedefler) {
            if (dugum instanceof Image) {
                dugum.insertBefore(new Text("[görsel: " + altMetin(dugum) + "]"));
            } else {
                Node cocuk = dugum.getFirstChild();
                while (cocuk != null) {               // bağlantı METNİ (biçimiyle) yerinde kalır
                    Node sonraki = cocuk.getNext();
                    dugum.insertBefore(cocuk);
                    cocuk = sonraki;
                }
            }
            dugum.unlink();
        }
        return belge;
    }

    private static String altMetin(Node gorsel) {
        StringBuilder sb = new StringBuilder();
        gorsel.accept(new AbstractVisitor() {
            @Override
            public void visit(Text metin) {
                sb.append(metin.getLiteral());
            }
        });
        return sb.toString();
    }
}
