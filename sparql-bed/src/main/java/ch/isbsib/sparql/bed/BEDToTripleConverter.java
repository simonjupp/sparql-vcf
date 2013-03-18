package ch.isbsib.sparql.bed;

import java.util.ArrayList;
import java.util.List;

import org.broad.tribble.Feature;
import org.broad.tribble.annotation.Strand;
import org.broad.tribble.bed.BEDFeature;
import org.broad.tribble.bed.FullBEDFeature.Exon;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

public class BEDToTripleConverter {
	public BEDToTripleConverter(ValueFactory vf) {
		super();
		this.vf = vf;
	}

	private final ValueFactory vf;

	public List<Statement> convertLineToTriples(String filePath,
			Feature feature, long lineNo) {
		List<Statement> stats = new ArrayList<Statement>(28);
		String recordPath = filePath + '/' + lineNo;
		URI recordId = vf.createURI(recordPath);
		add(stats, recordId, BED.CHROMOSOME, feature.getChr());
		add(stats, recordId, RDF.TYPE, BED.FEATURE_CLASS);
		add(stats, recordId, RDF.TYPE, FALDO.REGION_CLASS);
		URI alignStartId = vf.createURI(recordPath + "#start");
		add(stats, recordId, FALDO.BEGIN_PREDICATE, alignStartId);
		add(stats, alignStartId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
		add(stats, alignStartId, FALDO.POSTION_PREDICATE, feature.getStart());
		add(stats, alignStartId, FALDO.REFERENCE_PREDICATE, feature.getChr());
		URI alignEndId = vf.createURI(recordPath + "#end");
		add(stats, recordId, FALDO.END_PREDICATE, alignEndId);
		add(stats, alignEndId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
		add(stats, alignEndId, FALDO.POSTION_PREDICATE, feature.getEnd());
		add(stats, alignEndId, FALDO.REFERENCE_PREDICATE, feature.getChr());
		if (feature instanceof BEDFeature) {
			stats.addAll(convertLineToTriples(filePath, (BEDFeature) feature,
					lineNo));
		}
		return stats;
	}

	private List<Statement> convertLineToTriples(String filePath,
			BEDFeature feature, long lineNo) {
		List<Statement> stats = new ArrayList<Statement>(28);
		String recordPath = filePath + '/' + lineNo;
		URI recordId = vf.createURI(recordPath);
		if (feature.getName() != null) // name
			add(stats, recordId, RDFS.LABEL, feature.getName());
		if (feature.getScore() != Float.NaN) // score
			add(stats, recordId, BED.SCORE, feature.getScore());
		addStrandedNessInformation(stats, feature, recordId);
		// we skip position 678 as these are colouring instructions

		for (Exon exon : feature.getExons()) {

			String exonPath = recordPath + "/exon/" + exon.getNumber();
			URI exonId = vf.createURI(exonPath);
			URI beginId = vf.createURI(exonPath + "/begin");
			URI endId = vf.createURI(exonPath + "/end");
			add(stats, recordId, BED.EXON, endId);
			add(stats, exonId, RDF.TYPE, FALDO.REGION_CLASS);
			add(stats, exonId, FALDO.BEGIN_PREDICATE, beginId);
			add(stats, beginId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
			add(stats, beginId, FALDO.POSTION_PREDICATE, exon.getCdStart());
			add(stats, beginId, FALDO.REFERENCE_PREDICATE, feature.getChr());
			add(stats, exonId, FALDO.END_PREDICATE, endId);
			add(stats, endId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
			add(stats, endId, FALDO.POSTION_PREDICATE, exon.getCdEnd());
			add(stats, endId, FALDO.REFERENCE_PREDICATE, feature.getChr());
		}
		return stats;
	}

	protected void addStrandedNessInformation(List<Statement> statements,
			BEDFeature feature, URI alignEndId) {

		if (Strand.POSITIVE == feature.getStrand()) {
			add(statements, alignEndId, RDF.TYPE,
					FALDO.FORWARD_STRAND_POSITION_CLASS);
		} else if (Strand.NEGATIVE == feature.getStrand()) {
			add(statements, alignEndId, RDF.TYPE,
					FALDO.REVERSE_STRANDED_POSITION_CLASS);
		} else {
			add(statements, alignEndId, RDF.TYPE, FALDO.STRANDED_POSITION_CLASS);
		}

	}

	private void add(List<Statement> statements, URI subject, URI predicate,
			String string) {
		add(statements, subject, predicate, vf.createLiteral(string));

	}

	private void add(List<Statement> statements, URI subject, URI predicate,
			int string) {
		add(statements, subject, predicate, vf.createLiteral(string));

	}

	private void add(List<Statement> statements, URI subject, URI predicate,
			float string) {
		add(statements, subject, predicate, vf.createLiteral(string));

	}

	private void add(List<Statement> statements, Resource subject,
			URI predicate, Value object) {
		statements.add(vf.createStatement(subject, predicate, object));
	}
}