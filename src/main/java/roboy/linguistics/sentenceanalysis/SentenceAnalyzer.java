package roboy.linguistics.sentenceanalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import roboy.linguistics.Linguistics;
import roboy.linguistics.Linguistics.SENTENCE_TYPE;
import roboy.linguistics.Term;
import roboy.linguistics.Triple;
import roboy.util.Maps;

/**
 * Tries to find triples with rather stupid heuristics and stores the results in the
 * Linguistics.TRIPLE attribute of the interpretation.
 * 
 */
public class SentenceAnalyzer implements Analyzer{
	
	private Map<String,String> meanings;
	
	public SentenceAnalyzer () throws IOException, JsonSyntaxException, JsonIOException {
		// find meaning
		meanings = new HashMap<>();
		ClassLoader cl = this.getClass().getClassLoader();


		List<String> filenames = new ArrayList<>();
		String path = "ontology/terms/";
		Gson gson = new Gson();

		try(
			InputStream inputStream = cl.getResourceAsStream(path);
			BufferedReader br = new BufferedReader( new InputStreamReader( inputStream ) ) ) {
			String resource;

			while( (resource = br.readLine()) != null ) {
				InputStream is2 = cl.getResourceAsStream(path+resource);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));
				Term t = gson.fromJson(br2,Term.class);
				meanings.put(resource,t.concept);
				filenames.add( resource );
			}
		}
	}
	
	@Override
	public Interpretation analyze(Interpretation interpretation){
		String sentence = (String) interpretation.getFeatures().get(Linguistics.SENTENCE);
		// NLP pipeline
		String[] tokens = (String[]) interpretation.getFeatures().get(Linguistics.TOKENS);
		String[] posTags = (String[]) interpretation.getFeatures().get(Linguistics.POSTAGS);
		SENTENCE_TYPE sentenceType = interpretation.getSentenceType();
		
		Interpretation result = extractPAS(sentence, tokens, posTags, sentenceType); //TODO: avoid object creation
		interpretation.getFeatures().putAll(result.getFeatures());
		return interpretation;
	}
	
	
	private Interpretation extractPAS(String sentence, String[] tokens, String[] posTags, SENTENCE_TYPE sentenceType){
//		System.out.println("  "+sentenceType);
		switch(sentenceType){
			case STATEMENT: return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeStatement(tokens, posTags)));
			case IS_IT:     return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeIsIt(tokens, posTags)));
			case WHO:       return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeWho(tokens, posTags)));
			case HOW_IS:       return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeHowIs(tokens, posTags)));
			case HOW_DO:       return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeHowDo(tokens, posTags)));
			case WHAT:       return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeWhat(tokens, posTags)));
			case DOES_IT:       return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,analyzeDoesIt(tokens, posTags)));
			default:        return new Interpretation(sentenceType,Maps.stringObjectMap(Linguistics.SENTENCE,sentence,Linguistics.TRIPLE,new Triple(null,"","")));
		}
	}
	
	private Triple analyzeStatement(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		for(int i=0; i<tokens.length; i++){
			if(posTags[i].startsWith("V")){
				for(int j=0; j<i; j++){
					if(posTags[j].startsWith("N")||posTags[j].startsWith("J")){
						if(j>0) agens+=" ";
						agens+=tokens[j];
					}
				}
				predicate = tokens[i];
				for(int j=i+1; j<tokens.length; j++){
					if(posTags[j].startsWith("N")||posTags[j].startsWith("J")){
						if(j>i+1) patiens+=" ";
						patiens+=tokens[j];
					}
				}
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeIsIt(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>2){
			predicate = tokens[0];
			agens = tokens[1];
			for(int j=2; j<tokens.length; j++){
				if(j>2) patiens+=" ";
				patiens+=tokens[j];
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeDoesIt(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>3 && (posTags[3].startsWith("V") || posTags[2].startsWith("V"))){
			predicate = posTags[3].startsWith("V") ? tokens[3] : tokens[2];
			agens = tokens[1];
			for(int j=3; j<tokens.length; j++){
				if(j>3) patiens+=" ";
				patiens+=tokens[j];
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeWho(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>2 && posTags[1].startsWith("V")){
			patiens = null;
			predicate = tokens[1];
			for(int j=2; j<tokens.length; j++){
				if(j>2) agens+=" ";
				agens+=tokens[j];
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeWhat(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>3 && posTags[3].startsWith("V")){
			agens = tokens[2];
			predicate = tokens[3];
			patiens = null;
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeHowIs(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>2 && posTags[1].startsWith("V")){
			patiens = null;
			predicate = tokens[1];
			for(int j=2; j<tokens.length; j++){
				if(j>2) agens+=" ";
				agens+=tokens[j];
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
	private Triple analyzeHowDo(String[] tokens, String[] posTags){
		String predicate = null;
		String agens = "";
		String patiens = "";
		if(tokens.length>3 && posTags[3].startsWith("V")){
			agens = tokens[2];
			predicate = tokens[3];
			for(int j=4; j<tokens.length; j++){
				if(j>4) patiens+=" ";
				patiens+=tokens[j];
			}
		}
		return new Triple(predicate,agens,patiens);
	}
	
//	private int detectNP(String[] posTags){
//		
//	}
	
//	private int detectHeadVerb(String[] posTags){
//		
//	}
}
