package thunder.hack.cmd.impl;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.report.AbuseReportContext;
import net.minecraft.client.report.ReporterEnvironment;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Uuids;
import thunder.hack.cmd.Command;
import thunder.hack.injection.accesors.IMinecraftClient;

import java.util.Optional;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class LoginCommand extends Command {
    public LoginCommand() {
        super("login");
    }

    @Override
    public void executeBuild(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("name", StringArgumentType.word()).executes(context -> {
            login(context.getArgument("name", String.class));
            sendMessage("Аккаунт изменен на: " + MC.getSession().getUsername());

            return SINGLE_SUCCESS;
        }));

        builder.executes(context -> {
            sendMessage("Использование: .login <nickname>");

            return SINGLE_SUCCESS;
        });
    }

    public void login(String name) {
        try {
            setSession(new Session(name, Uuids.getOfflinePlayerUuid(name).toString(), "", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG));
        } catch (Exception exception) {
            sendMessage("Неверное имя! " + exception);
        }
    }


    public void setSession(Session session) {
        IMinecraftClient mca = (IMinecraftClient) MC;
        mca.setSessionT(session);
        MC.getSessionProperties().clear();
        UserApiService apiService;
        apiService = UserApiService.OFFLINE;
        mca.setUserApiService(apiService);
        mca.setSocialInteractionsManagerT(new SocialInteractionsManager(MC, apiService));
        mca.setProfileKeys(ProfileKeys.create(apiService, session, MC.runDirectory.toPath()));
        mca.setAbuseReportContextT(AbuseReportContext.create(ReporterEnvironment.ofIntegratedServer(), apiService));
    }
}
