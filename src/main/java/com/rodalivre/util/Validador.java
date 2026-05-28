package com.rodalivre.util;

import java.util.regex.Pattern;

public class Validador {

    // Regex para Placa Clássica Nacional (AAA-9999 ou AAA9999)
    private static final Pattern PLACA_CLASSICA_PATTERN = Pattern.compile("^[A-Z]{3}-?\\d{4}$");
    
    // Regex para Placa Mercosul (AAA9A99)
    private static final Pattern PLACA_MERCOSUL_PATTERN = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");

    /**
     * Valida placa de veículo (Padrão clássico nacional ou Mercosul)
     */
    public static boolean isPlacaValida(String placa) {
        if (placa == null) return false;
        String placaUpper = placa.trim().toUpperCase();
        return PLACA_CLASSICA_PATTERN.matcher(placaUpper).matches() || 
               PLACA_MERCOSUL_PATTERN.matcher(placaUpper).matches();
    }

    /**
     * Valida CNH (Exige exatamente 11 caracteres numéricos)
     */
    public static boolean isCnhValida(String cnh) {
        if (cnh == null) return false;
        String cleanCnh = cnh.replaceAll("\\D", "");
        return cleanCnh.length() == 11 && !cleanCnh.matches("^(\\d)\\1{10}$");
    }

    /**
     * Valida CPF com algoritmo matemático de dígitos verificadores
     */
    public static boolean isCpfValido(String cpf) {
        if (cpf == null) return false;
        
        // Remove caracteres não numéricos
        String cleanCpf = cpf.replaceAll("\\D", "");

        // CPF deve ter exatamente 11 dígitos e não ter dígitos todos iguais
        if (cleanCpf.length() != 11 || cleanCpf.matches("^(\\d)\\1{10}$")) {
            return false;
        }

        try {
            // Primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cleanCpf.charAt(i) - '0') * (10 - i);
            }
            int digito1 = 11 - (soma % 11);
            if (digito1 > 9) digito1 = 0;

            // Segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cleanCpf.charAt(i) - '0') * (11 - i);
            }
            int digito2 = 11 - (soma % 11);
            if (digito2 > 9) digito2 = 0;

            return (cleanCpf.charAt(9) - '0' == digito1) && (cleanCpf.charAt(10) - '0' == digito2);
        } catch (Exception e) {
            return false;
        }
    }
}
