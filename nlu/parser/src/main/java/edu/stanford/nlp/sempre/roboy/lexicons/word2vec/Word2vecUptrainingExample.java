package edu.stanford.nlp.sempre.roboy.lexicons.word2vec;

import org.deeplearning4j.models.embeddings.WeightLookupTable;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;

import java.util.Collection;


/**
 * Neural net that processes text into word-vectors.
 * This example shows how to save/load and train the model.
 *
 * Adapted from org.deeplearning4j.examples.nlp.word2vec.Word2VecUptrainingExample
 */
public class Word2vecUptrainingExample {

    public static void main(String[] args) throws Exception {

        String modelPath = "./resources/word2vec_toy_data_and_model/raw_sentences.model";

        ToyDataGetter dataGetter = new ToyDataGetter(true);
        dataGetter.ensureToyDataIsPresent();
        String dataPath = dataGetter.getToyDataFilePath();

        // load and preprocess data
        System.out.println("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(dataPath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());

        // manual creation of VocabCache and WeightLookupTable usually isn't necessary
        // but in this case we'll need them
        VocabCache<VocabWord> cache = new AbstractCache<>();
        WeightLookupTable<VocabWord> table = new InMemoryLookupTable.Builder<VocabWord>()
                .vectorLength(100)
                .useAdaGrad(false)
                .cache(cache).build();

        System.out.println("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .epochs(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .lookupTable(table)
                .vocabCache(cache)
                .build();

        System.out.println("Fitting Word2Vec model....");
        vec.fit();


        Collection<String> lst = vec.wordsNearest("day", 10);
        System.out.println("Closest words to 'day' on 1st run: " + lst);

        /*
            at this moment we're supposed to have model built, and it can be saved for future use.
         */

        WordVectorSerializer.writeWord2VecModel(vec, modelPath);

        /*
            Let's assume that some time passed, and now we have new corpus to be used to weights update.
            Instead of building new model over joint corpus, we can use weights update mode.
         */
        Word2Vec word2Vec = WordVectorSerializer.readWord2VecModel(modelPath);

        /*
            PLEASE NOTE: after model is restored, it's still required to set SentenceIterator and TokenizerFactory, if you're going to train this model
         */
        SentenceIterator iterator = new BasicLineIterator(dataPath);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        word2Vec.setTokenizerFactory(tokenizerFactory);
        word2Vec.setSentenceIterator(iterator);


        System.out.println("Word2vec uptraining...");

        word2Vec.fit();

        lst = word2Vec.wordsNearest("day", 10);
        System.out.println("Closest words to 'day' on 2nd run: " + lst);

        /*
            Model can be saved for future use now
         */

    }
}