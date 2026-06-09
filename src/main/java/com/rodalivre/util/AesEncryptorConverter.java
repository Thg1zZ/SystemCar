package com.rodalivre.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM_GCM = "AES/GCM/NoPadding";
    private static final String ALGORITHM_CBC = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptorConverter() {
        // Leitura da env var no construtor (não como static final) para garantir que
        // o Render e outros ambientes cloud já tenham as variáveis de ambiente disponíveis.
        String secretKeyEnv = System.getenv("DB_ENCRYPTION_KEY");
        if (secretKeyEnv == null || secretKeyEnv.length() < 32) {
            throw new IllegalStateException(
                "[SEGURANÇA/LGPD] A variável de ambiente DB_ENCRYPTION_KEY não está configurada " +
                "ou possui menos de 32 caracteres. Esta chave é obrigatória para criptografar " +
                "dados sensíveis (CPF/CNH) conforme LGPD e ISO 27001. " +
                "Configure a variável de ambiente antes de iniciar a aplicação."
            );
        }
        byte[] keyBytes = secretKeyEnv.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        this.keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }


    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Gerando um IV aleatório de 12 bytes para GCM (tamanho recomendado)
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            javax.crypto.spec.GCMParameterSpec parameterSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Concatenando IV (12 bytes) + Dados Criptografados
            byte[] combined = new byte[iv.length + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar atributo sensível para persistência (LGPD/ISO 27001)", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            
            // Tenta decodificar primeiro usando o novo padrao AES-GCM (IV de 12 bytes)
            try {
                if (combined.length < 12) {
                    throw new IllegalArgumentException("Dados muito curtos para AES-GCM");
                }
                
                byte[] iv = new byte[12];
                System.arraycopy(combined, 0, iv, 0, 12);
                javax.crypto.spec.GCMParameterSpec parameterSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);

                int encryptedSize = combined.length - 12;
                byte[] encryptedData = new byte[encryptedSize];
                System.arraycopy(combined, 12, encryptedData, 0, encryptedSize);

                Cipher cipher = Cipher.getInstance(ALGORITHM_GCM);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
                byte[] decrypted = cipher.doFinal(encryptedData);

                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception gcmException) {
                // Fallback robusto para descriptografia AES-CBC antiga (IV de 16 bytes) se falhar
                if (combined.length < 16) {
                    throw new IllegalArgumentException("Dados criptografados inválidos ou corrompidos");
                }

                byte[] iv = new byte[16];
                System.arraycopy(combined, 0, iv, 0, 16);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);

                int encryptedSize = combined.length - 16;
                byte[] encryptedData = new byte[encryptedSize];
                System.arraycopy(combined, 16, encryptedData, 0, encryptedSize);

                Cipher cipher = Cipher.getInstance(ALGORITHM_CBC);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                byte[] decrypted = cipher.doFinal(encryptedData);

                return new String(decrypted, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar atributo sensível do banco de dados (LGPD/ISO 27001)", e);
        }
    }
}
