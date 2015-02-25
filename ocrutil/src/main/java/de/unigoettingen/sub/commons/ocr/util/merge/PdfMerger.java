package de.unigoettingen.sub.commons.ocr.util.merge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;

public class PdfMerger implements Merger {

	@Override
	@SuppressWarnings("unchecked")
	public void merge(List<InputStream> inputs, OutputStream output) {
		try {
			// Stolen from itext (com.lowagie.tools.concat_pdf)

			int pageOffset = 0;
			List<HashMap<String, Object>> master = new ArrayList<HashMap<String, Object>>();
			int f = 0;
			Document document = null;
			PdfCopy writer = null;
			while (f < inputs.size()) {
				// we create a reader for a certain document
				PdfReader reader = new PdfReader(inputs.get(f));
				reader.consolidateNamedDestinations();
				// we retrieve the total number of pages
				int n = reader.getNumberOfPages();
				List<HashMap<String, Object>> bookmarks = SimpleBookmark
						.getBookmark(reader);
				if (bookmarks != null) {
					if (pageOffset != 0) {
						SimpleBookmark
								.shiftPageNumbers(bookmarks, pageOffset, null);
					}
					master.addAll(bookmarks);
				}
				pageOffset += n;

				if (f == 0) {
					// step 1: creation of a document-object
					document = new Document(reader.getPageSizeWithRotation(1));
					// step 2: we create a writer that listens to the document
					writer = new PdfCopy(document, output);
					// step 3: we open the document
					document.open();
				}
				// step 4: we add content
				PdfImportedPage page;
				for (int i = 0; i < n;) {
					++i;
					page = writer.getImportedPage(reader, i);
					writer.addPage(page);
				}
				writer.freeReader(reader);
				f++;
			}
			if (!master.isEmpty()) {
				writer.setOutlines(master);
			}
			// step 5: we close the document
			document.close();
		} catch (IOException e) {
			throw new IllegalStateException("Error while merging files.", e);
		} catch (DocumentException e) {
			throw new IllegalStateException("Error while merging files.", e);
		}

	}

}
