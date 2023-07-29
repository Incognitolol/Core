package rip.alpha.core.bukkit.totp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.Getter;
import rip.alpha.core.shared.server.NetworkServerHandler;
import rip.alpha.core.shared.server.NetworkServerType;
import rip.alpha.libraries.util.imgur.ImgurUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Getter
public class TotpHandler {

    @Getter
    private static final TotpHandler instance = new TotpHandler();

    @Getter
    private final boolean enabled = NetworkServerHandler.getInstance().getCurrentServer().getServerType() != NetworkServerType.DEVELOPMENT;
    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final JsonParser parser;

    private final TimeProvider timeProvider;
    private final CodeGenerator codeGenerator;
    private final DefaultCodeVerifier codeVerifier;

    private TotpHandler() {
        this.secretGenerator = new DefaultSecretGenerator();
        this.qrGenerator = new ZxingPngQrGenerator();

        //Generator & provider
        this.timeProvider = new SystemTimeProvider();
        this.codeGenerator = new DefaultCodeGenerator();
        this.codeVerifier = new DefaultCodeVerifier(this.codeGenerator, this.timeProvider);
        this.parser = new JsonParser();
    }

    protected QrData createQRData(String username, String secret) {
        return new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer("AlphaMC Network (In-game)")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
    }

    protected String uploadQRImage(QrData qrData) throws QrGenerationException, IOException {
        byte[] data = this.qrGenerator.generate(qrData);
        File tempFile = File.createTempFile(qrData.getLabel(), "png");
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        outputStream.write(data);
        outputStream.close();
        String uploadJsonString = ImgurUtil.upload(tempFile);
        JsonObject jsonObject = this.parser.parse(uploadJsonString).getAsJsonObject();
        JsonObject dataObject = jsonObject.getAsJsonObject("data");
        tempFile.deleteOnExit();
        return dataObject.get("link").getAsString();
    }
}
