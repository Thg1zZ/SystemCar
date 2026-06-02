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

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String SECRET_KEY_ENV = System.getenv("DB_ENCRYPTION_KEY");
    private static final String DEFAULT_KEY = "SystemCarSecretKeyForLGPD_2026!"; // Chave padrão simétrica

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesEncryptorConverter() {
        byte[] keyBytes;
        if (SECRET_KEY_ENV != null && SECRET_KEY_ENV.length() >= 32) {
            keyBytes = SECRET_KEY_ENV.substring(0, 32).getBytes(StandardCharsets.UTF_8);
        } else {
            keyBytes = DEFAULT_KEY.getBytes(StandardCharsets.UTF_8);
        }
        this.keySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            // Gerando um IV aleatório de 16 bytes (padrão do AES/CBC)
            byte[] iv = new byte[16];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedData = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            // Concatenando IV (16 bytes) + Dados Criptografados
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
            if (combined.length < 16) {
                throw new IllegalArgumentException("Dados criptografados inválidos ou corrompidos");
            }

            // Extraindo o IV de 16 bytes do início
            byte[] iv = new byte[16];
            System.arraycopy(combined, 0, iv, 0, 16);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Extraindo o restante dos bytes correspondentes ao texto cifrado
            int encryptedSize = combined.length - 16;
            byte[] encryptedData = new byte[encryptedSize];
            System.arraycopy(combined, 16, encryptedData, 0, encryptedSize);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encryptedData);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao descriptografar atributo sensível do banco de dados (LGPD/ISO 27001)", e);
        }
    }
}
