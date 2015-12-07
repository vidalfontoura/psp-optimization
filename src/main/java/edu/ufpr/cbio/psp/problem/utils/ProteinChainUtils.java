package edu.ufpr.cbio.psp.problem.utils;

public class ProteinChainUtils {

    public static String getNotationByProteinChain(String proteinChain) {

        char lastChar = proteinChain.charAt(0);
        int count = 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < proteinChain.length(); i++) {
            char charAt = proteinChain.charAt(i);
            if (lastChar == charAt) {
                count++;
            } else {
                sb.append(lastChar);
                if (count > 1) {
                    sb.append(count);
                }
                count = 1;
            }
            lastChar = charAt;
        }
        sb.append(lastChar);
        if (count > 1) {
            sb.append(count);
        }

        return sb.toString();
    }

    public static String get(String proteinChain) {

        char lastChar = proteinChain.charAt(0);
        int count = 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < proteinChain.length(); i++) {
            char charAt = proteinChain.charAt(i);
            if (lastChar == charAt) {
                count++;
            } else {
                sb.append(lastChar);
                if (count > 1) {
                    sb.append(count);
                }
                count = 1;
            }
            lastChar = charAt;
        }
        sb.append(lastChar);
        if (count > 1) {
            sb.append(count);
        }

        return sb.toString();
    }

    public static void main(String[] args) {

        String notationChain = ProteinChainUtils.getNotationByProteinChain("HHHHHHPPPPPPHHPPHPHHPPH");
        System.out.println(notationChain);

        System.out.println(ProteinChainUtils.get("H6P6H2P2HPH2P2H"));

    }
}
