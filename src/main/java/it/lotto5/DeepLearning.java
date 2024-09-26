package it.lotto5;

import it.eng.pilot.PList;
import it.eng.pilot.PilotSupport;
import it.lotto5.dto.Estrazione5Minuti;
import org.apache.log4j.BasicConfigurator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class DeepLearning extends PilotSupport {
    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        DeepLearning dp = new DeepLearning();
        PList<PList<Integer>> dataSet = dp.leggiDati();

        int numSequences = 2880;
        int sequenceLength = 20;
        int numFeatures = 1;

        // Creazione delle matrici features e labels
        INDArray features = Nd4j.create(numSequences, sequenceLength, numFeatures);
        INDArray labels = Nd4j.create(numSequences, numFeatures);


        for (int i = 0; i < numSequences; i++) {
            PList<Integer> lista = dataSet.get(i);
            for (int j = 0; j < sequenceLength; j++) {
                features.put(i, j, lista.get(j));
            }
            // Assegnazione di una label casuale (sostituisci con le tue labels reali)
            int randomClass = (int) (Math.random() * 1);
            labels.put(i, 1, 1.0);
        }


        // Popolamento delle matrici con dati casuali (one-hot encoding)
        // ... (codice per popolare le matrici con one-hot encoding)

        // Configurazione della rete neurale
        /*
        MultiLayerConfiguration conf = new NeuralNetConfiguration().Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .list()
                .layer(0, new LSTM().Builder().nIn(numFeatures).nOut(64).activation(Activation.TANH).build())
                .layer(1, new DenseLayer().Builder().nOut(numFeatures).activation(Activation.SOFTMAX).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();


        // Creazione di un DataSet
        DataSet dataSet1 = new DataSet(features, labels);

        // Addestramento della rete
        model.fit(dataSet1);

        // Predizione della prossima sequenza
        // ... (codice per generare una nuova sequenza e fare la predizione)
*/

    }

    private PList<PList<Integer>> leggiDati() throws IOException {
        PList<PList<Integer>> dataSet = pl();
        Set<File> files = getFiles("estrazioni", null, null, ".txt", now().toString(), false);
        for (File f : files) {
            PList<String> cont = readFile(f.getPath());
            for (String s : cont) {
                dataSet.add(new Estrazione5Minuti(s).getEstrazione());
            }
        }
        return dataSet;
    }

}