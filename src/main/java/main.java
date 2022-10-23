import java.io.*;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main {

//哪些情况下，无需DESCRIPTION 辅助也可以？LIKE TRUE/FALSE assertions -> 降低我们的方法对于 DESCRIPTION 的依赖，用 CLASSIFER
//整理APACHE 2个库手写的测试用例，想得到的是：有多少比例的ASSERT NULL, NOT NULL, VS 别的ASSERTIONS
// "blah " ".getDescription()"

    public static void processLayerDir(File dir, PrintWriter writer) {
        File[] listToGo = dir.listFiles();
        System.out.println("Processing" + dir.getName());
        System.out.println(Arrays.toString(listToGo));
        if (listToGo != null) {
            for (File child : listToGo) {
                System.out.println(child.getName() +" " + child.isDirectory());
                if (child.isDirectory()) {
                    processLayerDir(child, writer);
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(new FileReader(child.getAbsolutePath()))){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains("assert") && !(line.toLowerCase().contains("import"))) {
                            line = line.trim();
                            line = line.replace("\n", "");
                            if (line.contains(";")) {
                                writer.println(line);
                            } else {
                                while (!(line.contains(";")) || line.contains("}")) {
                                    String nextLine;
                                    if ((nextLine = reader.readLine()) != null) {
                                        line = line.concat(nextLine.trim());
                                    } else {
                                        break;
                                    }
                                }
                                line = line.trim();
                                line = line.replace("\n", "");
                                writer.println(line);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("not a directory");
        }
    }

    public static void countAssertionOfDifferenceTypes(String fileName, PrintWriter w1, PrintWriter w2, PrintWriter w3, PrintWriter w4) {
        File assertionsToProcess = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(assertionsToProcess.getAbsolutePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("assertnull") || line.toLowerCase().contains("assertnotnull")) {
                    w1.println(line);
                } else if (line.toLowerCase().contains("asserttrue") || line.toLowerCase().contains("assertfalse")) {
                    w2.println(line);
                } else if (line.toLowerCase().contains("assertthrow")) {
                    w3.println(line);
                } else {
                    w4.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int countFreqOfComma(String s) {
        Pattern pattern = Pattern.compile("[^e]*e");
        Matcher matcher = pattern.matcher(s);
        int count = 0;
        while (matcher.find()) {
            count ++;
        }
        return count;
    }

    public static void main(String[] args) throws IOException {
        String fileName = "";
        //String outputFileName = "extractedAssertions.txt";
        String outputFileName = "extractedAssertions2.txt";
        //step 1
        //String sampleDirectory = "./samples/commons-lang/src/test/java/org/apache/commons/lang3";
        String sampleDirectory = "samples/commons-io/src/test/java/org/apache/commons/io";
        File dirToProcess = new File(sampleDirectory);
        //PrintWriter writer = new PrintWriter(new FileWriter(outputFileName));
        // Step 1
        //processLayerDir(dirToProcess, writer);

        // Step 2: count the number of simplistic(short) assertions VS assertions with business logics
        String assertions1 = "extractedAssertions.txt";
        String assertions2 = "extractedAssertions2.txt";

        String output1NullNotNull = "nullNotNull.txt";
        String output2TrueFalse = "trueFalse.txt";
        String output3Exceptional = "exceptional.txt";
        String output4PotentiallyComplex = "equalsThatArrayEqualsSame.txt";
        //PrintWriter w1 = new PrintWriter(new FileWriter(output1NullNotNull));
        //PrintWriter w2 = new PrintWriter(new FileWriter(output2TrueFalse));
        //PrintWriter w3 = new PrintWriter(new FileWriter(output3Exceptional));
        //PrintWriter w4 = new PrintWriter(new FileWriter(output4PotentiallyComplex));
        //countAssertionOfDifferenceTypes(assertions1, w1, w2, w3, w4);
        //countAssertionOfDifferenceTypes(assertions2, w1, w2, w3, w4);

        // string + '.'
        // [] + '.'
        // [] + number (0-9)
        // string + number
        // contains 4 or more commas
        // long lines
        int border = ("                                                                                            " +
                "                            ").length();
        // Step 3: Calculate the percentages of different types of assertions and print out
        String compiled = "Summary.txt";
        String complexAssertions = "complexAssertions_SubsetOfEquals.txt";
        PrintWriter assertWriter = new PrintWriter(new FileWriter(complexAssertions));
        PrintWriter compileWriter = new PrintWriter(new FileWriter(compiled));
        int countNullNotNull = 0;
        int countTrueFalse = 0;
        int countException = 0;
        int countPotentialComplex = 0;
        int countComplex = 0;
        try (BufferedReader r1 = new BufferedReader(new FileReader(output1NullNotNull));
        BufferedReader r2 = new BufferedReader(new FileReader(output2TrueFalse));
        BufferedReader r3 = new BufferedReader(new FileReader(output3Exceptional));
        BufferedReader r4 = new BufferedReader(new FileReader(output4PotentiallyComplex))){
            String line1;
            String line2;
            String line3;
            String line4;
            while ((line1 = r1.readLine()) != null) {
                countNullNotNull ++;
            }
            while ((line2 = r2.readLine()) != null) {
                countTrueFalse ++;
            }
            while ((line3 = r3.readLine()) != null) {
                countException ++;
            }
            while ((line4 = r4.readLine()) != null) {
                countPotentialComplex ++;
                if (line4.contains("\"") && line4.contains(".")) {
                    countComplex ++;
                    assertWriter.println(line4);
                } else if (line4.contains("array") && line4.contains(".")) {
                    countComplex ++;
                    assertWriter.println(line4);
                } else if (line4.contains("\"") && line4.matches(".*[0-9].*")) {
                    countComplex ++;
                    assertWriter.println(line4);
                } else if (line4.contains("array")  && line4.matches(".*[0-9].*")) {
                    countComplex ++;
                    assertWriter.println(line4);
                } else if (countFreqOfComma(line4) >= 4) {
                    countComplex ++;
                    assertWriter.println(line4);
                } else if (line4.length() >= border) {
                    countComplex ++;
                    assertWriter.println(line4);
                }
            }
            int summ = countNullNotNull + countTrueFalse + countException + countPotentialComplex;
            float percentNull = (float) countNullNotNull/summ;
            float percentBoolean = (float) countTrueFalse/summ;
            float percentException = (float) countException/summ;
            float percentComplex = (float) countComplex/summ;
            compileWriter.println("Total number of assertions is " + summ);
            compileWriter.println("Null or NotNull assertions are total:" + countNullNotNull + " percentage " + percentNull);
            compileWriter.println("Bool assertions are: " + countTrueFalse + " percentage " + percentBoolean);
            compileWriter.println("Exceptional assertions are: " + countException + " percentage " + percentException);
            compileWriter.println("Complex assertions (among equals) are: " + countComplex + " percentage " + percentComplex);
            assertWriter.close();
            compileWriter.close();
        }
    }
}
