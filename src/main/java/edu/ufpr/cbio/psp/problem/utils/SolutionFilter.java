package edu.ufpr.cbio.psp.problem.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.uma.jmetal.core.Solution;
import org.uma.jmetal.core.SolutionSet;
import org.uma.jmetal.core.Variable;
import org.uma.jmetal.encoding.variable.Int;
import org.uma.jmetal.util.JMetalLogger;

import edu.ufpr.cbio.psp.problem.PSPProblem;

public class SolutionFilter {
	
	/**
	 * This method group the solutions for the I sequences, J algorithms and K executions creating the 
	 * FUN_ALL and VAR_ALL files.
	 */
	public void groupSolutions() {
		int numSequences = 7;
		String algorithms[] = {"IBEA", "M_IBEA", "NSGAII", "M_NSGAII"};
		int executions = 30;
		
		SolutionSet solutionSet = new SolutionSet();
		String path = "";
		for(int i=0; i<numSequences; i++) {
			for(int j=0; j < algorithms.length; j++) {
				solutionSet.clear();
				for(int k=0; k<executions; k++) {
					path = "../psp-2D/sq"+(i+1)+"/"+algorithms[j]+"/EXECUTION_"+k+"/";
					
					File fileFun = new File(path + File.separator + "FUN.txt");
					File fileVar = new File(path + File.separator + "VAR.txt");
					if (fileFun.exists() && fileVar.exists()) {
						SolutionSet s = readSolutionSet(fileFun);
						List<Int[]> v = readDecisionVariableSet(fileVar);
						for(int l=0; l<s.size(); l++) {
							solutionSet.setMaximumSize(solutionSet.getMaximumSize() + 1);
							solutionSet.add(s.get(l));
							solutionSet.get(solutionSet.size()-1).setDecisionVariables(v.get(l));
						}
					}
				
				}
				path = "../psp-2D/sq"+(i+1)+"/"+algorithms[j]+"/";
				PSPProblem problem = new PSPProblem("aaa", 2, 0, System.out);
				solutionSet = problem.removeDominateds(solutionSet);
				solutionSet = problem.removeDuplicates(solutionSet);
				writeFunAllFile(path, solutionSet);
				writeVarAllFile(path, solutionSet);
			}
		}
	}

	public SolutionSet readSolutionSet(File file) {
		try (BufferedReader br = new BufferedReader( new FileReader(file))) {

			SolutionSet solutionSet = new SolutionSet();

			String aux = br.readLine();
			while (aux != null) {
				StringTokenizer st = new StringTokenizer(aux);
				int i = 0;
				Solution solution = new Solution(st.countTokens());
				while (st.hasMoreTokens()) {
					double value = new Double(st.nextToken());
					solution.setObjective(i, value);
					i++;
				}
				solutionSet.setMaximumSize(solutionSet.getMaximumSize() + 1);
				solutionSet.add(solution);
				aux = br.readLine();
			}
			return solutionSet;
		} catch (Exception e) {
			JMetalLogger.logger.log(Level.SEVERE,
					"org.uma.jmetal.qualityindicator.util.readNonDominatedSolutionSet: " + file.getAbsolutePath(), e);
		}
		return null;
	}

	public List<Int[]> readDecisionVariableSet(File file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			List<Int[]> solutions = new ArrayList<>();
			
			
			String aux = br.readLine();
			while (aux != null) {
				String solution[] = aux.split(" ");
				Int v[] = new Int[solution.length];
				for(int i=0 ;i<solution.length; i++) {
					v[i] = new Int(Integer.parseInt(solution[i]), 0, 2);
				}
				solutions.add(v);
				aux = br.readLine();
			}
			
			br.close();
			return solutions;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void writeFunAllFile(String path, SolutionSet solutionSet) {
		try {
			FileOutputStream fis = new FileOutputStream(path+"FUN_ALL.txt");
			OutputStreamWriter isr = new OutputStreamWriter(fis);
			BufferedWriter bw = new BufferedWriter(isr);

			for(int i=0; i<solutionSet.size(); i++) {
				bw.write(solutionSet.get(i).getObjective(0)+" "+solutionSet.get(i).getObjective(1));
				bw.newLine();
			}
			bw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void writeVarAllFile(String path, SolutionSet solutionSet) {
		try {
			FileOutputStream fis = new FileOutputStream(path+"VAR_ALL.txt");
			OutputStreamWriter isr = new OutputStreamWriter(fis);
			BufferedWriter bw = new BufferedWriter(isr);
			
			for(int i=0; i<solutionSet.size(); i++) {
				Variable v[] = solutionSet.get(i).getDecisionVariables();
				for(int j=0; j<v.length; j++) {
					bw.write(v[j]+" ");
				}
				bw.newLine();
			}
			bw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
