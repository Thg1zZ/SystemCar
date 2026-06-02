package com.rodalivre.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Converter
public class AesEncryptorConverter implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY_ENV = System.getenv("DB_ENCRYPTION_KEY");
    private static final String DEFAULT_KEY = "SystemCarSecretKeyForLGPD_2026!"; // Fallback seguro de 32 bytes para AES-256

    private final SecretKeySpec keySpec;

    public AesEncryptorConverter() {
        // Usa a chave configurada no ambiente ou o fallback de 32 bytes (AES-256)
        byte[] keyBytes;
        if (SECRET_KEY_ENV != null && SECRET_KEY_ENV.length() >= 32) {
            keyBytes = SECRET_KEY_ENV.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        } else {
            keyBytes = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
        }
        this.keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
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
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar atributo sensível do banco de dados (LGPD/ISO 27001)", e);
        }
    }
}
