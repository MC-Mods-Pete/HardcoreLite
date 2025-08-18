package mars.somewhathardcore.com;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YouDieYouLoseAHeart implements ModInitializer {
	public static final String MOD_ID = "somewhathardcore";
	public static final Identifier HEALTH_GAINED = new Identifier(MOD_ID, "health_gained");
	public static final GameRules.Key<GameRules.IntRule> MAXIMUM_HEARTS =
			GameRuleRegistry.register("maxHearts", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(20));

	public static final GameRules.Key<GameRules.BooleanRule> CAN_RESTORE_HEARTS =
			GameRuleRegistry.register("canRestoreHearts", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

	@Override
	public void onInitialize() {

		ClientPlayNetworking.registerGlobalReceiver(YouDieYouLoseAHeart.HEALTH_GAINED, (client, handler, buf, responseSender) -> {
			int maxHealth = buf.readInt();
			int playerMaxHealth = buf.readInt();

			client.execute(() -> {
				client.player.sendMessage(Text.literal("Total health: " + maxHealth));
				client.player.sendMessage(Text.literal("Player specific health: " + playerMaxHealth));
			});
		});
	}
}