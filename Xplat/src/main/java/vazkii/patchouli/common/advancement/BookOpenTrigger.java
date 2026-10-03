package vazkii.patchouli.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// 26.3 把 advancements.criterion 拆成了 advancements.triggers（触发器）与 advancements.predicates
// （谓词），并去掉了 ContextAwarePredicate —— player 条件现在直接就是 Optional<Holder<LootItemCondition>>。
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import vazkii.patchouli.api.PatchouliAPI;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An advancement trigger for opening Patchouli books.
 */
public class BookOpenTrigger extends SimpleCriterionTrigger<BookOpenTrigger.TriggerInstance> {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(PatchouliAPI.MOD_ID, "open_book");
	public static final BookOpenTrigger INSTANCE = new BookOpenTrigger();

	@NotNull
	@Override
	public Codec<TriggerInstance> codec() {
		return BookOpenTrigger.TriggerInstance.CODEC;
	}

	public void trigger(@NotNull ServerPlayer player, @NotNull Identifier book) {
		trigger(player, instance -> instance.matches(book, null, 0));
	}

	public void trigger(@NotNull ServerPlayer player, @NotNull Identifier book, @Nullable Identifier entry, int page) {
		trigger(player, instance -> instance.matches(book, entry, page));
	}

	public record TriggerInstance(Optional<Holder<LootItemCondition>> player, Identifier book, Optional<Identifier> entry, MinMaxBounds.Ints page) implements SimpleInstance {

		public static Codec<BookOpenTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				Identifier.CODEC.fieldOf("book").forGetter(TriggerInstance::book),
				Identifier.CODEC.optionalFieldOf("entry").forGetter(TriggerInstance::entry),
				MinMaxBounds.Ints.CODEC.optionalFieldOf("page", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::page)
		).apply(instance, TriggerInstance::new));

		public boolean matches(@NotNull Identifier book, @Nullable Identifier entry, int page) {
			return this.book.equals(book) && (this.entry.isEmpty() || this.entry.get().equals(entry)) && this.page.matches(page);
		}
	}
}
