//index comparison,search code
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class indexComparison {
	public static void main(String args[]) throws IOException
	{	
		BufferedWriter bw = new BufferedWriter(new FileWriter("/Output/Vocabulary_StopAnalyzer.txt"));
		String indexpath="index/index_StopAnalyzer";
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get( (indexpath))));
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());
		bw.append("Total number of documents in the corpus: "+reader.maxDoc()); 
		bw.newLine();
	    //Print the number of documents containing the term "new" in <field>TEXT</field>.
		System.out.println("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
		bw.append("Number of documents containing the term \"new\" for field \"TEXT\": "+reader.docFreq(new Term("TEXT", "new")));
		bw.newLine();
	    //Print the total number of occurrences of the term "new" across all documents for <field>TEXT</field>.
		System.out.println("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
		bw.append("Number of occurrences of \"new\" in the field \"TEXT\": "+reader.totalTermFreq(new Term("TEXT","new")));
		bw.newLine();
	    Terms vocabulary = MultiFields.getTerms(reader, "TEXT");
	    //Print the size of the vocabulary for <field>TEXT</field>, applicable when the index has only one segment.
	    System.out.println("Size of the vocabulary for this field: "+vocabulary.size());
	    bw.append("Size of the vocabulary for this field: "+vocabulary.size());
		bw.newLine();
	    //Print the total number of documents that have at least one term for <field>TEXT</field>
		System.out.println("Number of documents that have at least one term for this field: "+vocabulary.getDocCount());
	    bw.append("Number of documents that have at least one term for this field: "+vocabulary.getDocCount()); 
		bw.newLine();
	    //Print the total number of tokens for <field>TEXT</field>
		System.out.println("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
	    bw.append("Number of tokens for this field: "+vocabulary.getSumTotalTermFreq());
		bw.newLine();
	    //Print the total number of postings for <field>TEXT</field>
		System.out.println("Number of postings for this field: "+vocabulary.getSumDocFreq());
	    bw.append("Number of postings for this field: "+vocabulary.getSumDocFreq()); 
		bw.newLine();
	    //Print the vocabulary for <field>TEXT</field> in a file
	    TermsEnum iterator = vocabulary.iterator();
	    BytesRef byteRef = null;
	    bw.append("\n*******Vocabulary-Start**********");
		bw.newLine();
	    while((byteRef = iterator.next()) != null) {
	    	String term = byteRef.utf8ToString();
	    	bw.append(term);
		bw.flush();
		bw.newLine();
	    }
	    bw.append("\n*******Vocabulary-End**********");  
		bw.newLine();
	    reader.close();	
	}
}
