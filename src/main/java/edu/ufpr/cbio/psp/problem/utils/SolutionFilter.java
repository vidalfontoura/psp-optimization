package edu.ufpr.cbio.psp.problem.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import edu.ufpr.cbio.psp.problem.domain.Residue;
import edu.ufpr.cbio.psp.problem.domain.Residue.Point;

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

	public void groupSolutionsIntoFile() {}
	
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
	
	public void groupSolutionsToFile() {
		int numSequences = 7;
		String algorithms[] = {"IBEA", "M_IBEA", "NSGAII", "M_NSGAII"};
		int executions = 30;
		String outputDirName = "groupedResults";
		
		File outputDir = new File(outputDirName);
		if(!outputDir.exists()) {
			outputDir.mkdir();
		}
		
		List<String[]> list = new ArrayList<>();
		
		String path = "";
		String fileName = "";
		for(int i=0; i<numSequences; i++) {
			for(int j=0; j < algorithms.length; j++) {
				List<String[]> aux = null;
				for(int k=0; k<executions; k++) {
					path = "../psp-2D/sq"+(i+1)+"/"+algorithms[j]+"/EXECUTION_"+k+"/";

					File fileFun = new File(path + File.separator + "FUN.txt");
					File fileVar = new File(path + File.separator + "VAR.txt");
					if (fileFun.exists() && fileVar.exists()) {
						aux = readSolutions(fileVar, fileFun, k+1);
						list.addAll(aux);
					}
				
				}
				path = outputDirName + File.separator;
				fileName = "sq"+(i+1)+"_"+algorithms[j];
				writeOutputFile(path, fileName, ",", list);
			}
		}
		System.out.println("Done.");
	}
	
	public List<String[]> readSolutions(File varFile, File funFile, int run) {
		BufferedReader var = null;
		BufferedReader fun = null;
		try {
			var = new BufferedReader( new FileReader(varFile));
			fun = new BufferedReader( new FileReader(funFile));
			
			List<String[]> list = new ArrayList<>();
			String auxVar = var.readLine();
			String auxFun = fun.readLine();
			
			while (auxVar != null && auxFun != null) {
				
				String[] str = new String[4];
				str[0] = String.valueOf(run);
				str[1] = auxVar.replace(" ", ",");
				str[2] = auxFun.split(" ")[0];
				str[3] = auxFun.split(" ")[1];
				
				list.add(str);
				
				auxVar = var.readLine();
				auxFun = fun.readLine();
			}
			
			var.close();
			fun.close();
			
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void writeOutputFile(String path, String fileName, String separator, List<String[]> list) {
		File outputFile = new File(path+fileName+".txt");

		try {
			
			FileOutputStream fis = new FileOutputStream(outputFile);
			OutputStreamWriter isr = new OutputStreamWriter(fis);
			BufferedWriter bw = new BufferedWriter(isr);
			
			for(int i=0; i<list.size(); i++) {
				String[] aux = list.get(i);
				bw.append(aux[0]+separator+aux[1]+aux[2]+separator+aux[3]);
				bw.newLine();
			}
			bw.close();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void fixNotRepairedSolutions() {
		String chains[] = {
				"HPHPPHHPHHPHPHHPPHPH",
				"HHPPHPPHPPHPPHPPHPPHPPHH",
				"PPHPPHHPPPPHHPPPPHHPPPPHH",
				"PPPHHPPHHPPPPPPHHHHHHHPPHHPPPPHHPPHPP",
				"PPHPPHHPPHHPPPPPHHHHHHHHHHPPPPPPHHPPHHPPHPPHHHHH",
				"HHPHPHPHPHHHHPHPPPHPPPHPPPPHPPPHPPPHPHHHHPHPHPHPHH",
				"PPHHHPHHHHHHHHPPPHHHHHHHHHHPHPPPHHHHHHHHHHHHPPPPHHHHHHPHHPHP",
				"HHHHHHHHHHHHPHPHPPHHPPHHPPHPPHHPPHHPPHPPHHPPHHPPHPHPHHHHHHHHHHHH"};
		int numSequences = chains.length;
		String algorithms[] = { "IBEA", "M_IBEA", "NSGAII", "M_NSGAII" };
		
		String inputPath = "../psp-2D/groupedResults";
		String outputPath = "fixedSolutions";
		for (int i = 0; i < numSequences; i++) {
			for (int j = 0; j < algorithms.length; j++) {
				File inputFile = new File(inputPath + File.separator + "sq" + (i + 1) + "_" + algorithms[j] + ".txt");
				if (inputFile.exists()) {
					List<String[]> s = readSolutionsFromFile(inputFile);
					for (String[] solution : s) {
						int[] vet = new int[solution.length - 3];
						for (int k = 1, l = 0; k < solution.length - 2; k++, l++) {
							vet[l] = Integer.parseInt(solution[k]);
						}
						String[] aux2 = repairSolution(chains[i], vet);
						for (int k = 1, l = 0; k < solution.length - 2; k++, l++) {
							solution[k] = aux2[l];
						}
					}
					
					File outputFile = new File(outputPath+File.separator+"sq" + (i + 1) + "_" + algorithms[j] + ".txt");
					writeSolutionToFile(outputFile, ",", s);
				} else {
					System.out.println("O arquivo nao existe.");
				}
			}
		}
		System.out.println("Feito.");
	}

	public String[] repairSolution(String chain, int[] solution) {

		Controller parser = new Controller();

		List<Residue> points = parser.parseInput(chain, solution);

		int prevDirection = 1; // Direção inicial
		int prevX = points.get(0).getPoint().getX(); // x inicial
		int prevY = points.get(0).getPoint().getY(); // y inicial

		String finalSolution = "";

		for (int i = 1; i < points.size(); i++) {
			Point p = points.get(i).getPoint();
			int currentX = p.getX();
			int currentY = p.getY();

			int x = currentX - prevX;
			int y = currentY - prevY;

			int currentDirection = 4; // Valor inicial
			if (x == 1 && y == 0) { // X atual está a direita de X anterior
				currentDirection = 1;
			} else if (x == -1 && y == 0) { // X atual está a esquerda de X
											// anterior
				currentDirection = 3;
			} else if (y == 1 && x == 0) { // Y atual está abaixo de Y anteior
				currentDirection = 2;
			} else if (y == -1 && x == 0) { // Y atual está acima de Y anterior
				currentDirection = 0;
			}

			int d = currentDirection - prevDirection;
			if (d == -1 || d == 3) {
				finalSolution += "2 ";
			} else if (d == 1 || d == -3) {
				finalSolution += "0 ";
			} else if (d == 0) {
				finalSolution += "1 ";
			}

			prevX = currentX;
			prevY = currentY;

			prevDirection = currentDirection;
		}
		return finalSolution.substring(2).split(" ");
	}

	public List<String[]> readSolutionsFromFile(File inputFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
			List<String[]> solutionSet = new ArrayList<>();
			String aux = br.readLine();
			while (aux != null) {
				String[] aux2 = aux.split(",");
				solutionSet.add(aux2);
				aux = br.readLine();
			}
			return solutionSet;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void writeSolutionToFile(File outputFile, String separator, List<String[]> solutions) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

			for (int i = 0; i < solutions.size(); i++) {
				String[] aux = solutions.get(i);
				for (int j = 0; j < aux.length - 1; j++) {
					bw.append(aux[j] + separator);
				}
				bw.append(aux[aux.length - 1]);
				bw.newLine();
			}
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		SolutionFilter sf = new SolutionFilter();
		sf.fixNotRepairedSolutions();
	}
}
