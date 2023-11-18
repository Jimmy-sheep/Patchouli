package vazkii.patchouli.client.handler;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.util.ItemStackUtil;

import org.jetbrains.annotations.Nullable;

public class BookRightClickHandler {

	public static void onRenderHUD(PoseStack ms, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		ItemStack bookStack = player.getMainHandItem();
		if (mc.screen == null) {
			Book book = ItemStackUtil.getBookFromStack(bookStack);

			if (book != null) {
				Pair<BookEntry, Integer> hover = getHoveredEntry(book);
				if (hover != null) {
					BookEntry entry = hover.getFirst();
					if (!entry.isLocked()) {
						Window window = mc.getWindow();
						int x = window.getGuiScaledWidth() / 2 + 3;
						int y = window.getGuiScaledHeight() / 2 + 3;
						entry.getIcon().render(ms, x, y);

						ms.pushPose();
						ms.translate(0, 0, 10);
						ms.scale(0.5F, 0.5F, 1);
						Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(ms, bookStack, (x + 8) * 2, (y + 8) * 2);
						ms.popPose();

						mc.font.draw(ms, entry.getName(), x + 18, y + 3, 0xFFFFFF);

						ms.pushPose();
						ms.scale(0.75F, 0.75F, 1F);
						Component s = Component.translatable("patchouli.gui.lexicon." + (player.isShiftKeyDown() ? "view" : "sneak"))
								.withStyle(ChatFormatting.ITALIC);
						mc.font.draw(ms, s, (x + 18) / 0.75F, (y + 14) / 0.75F, 0xBBBBBB);
						ms.popPose();
					}
				}
			}
		}
	}

	public static InteractionResult onRightClick(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
		ItemStack bookStack = player.getMainHandItem();

		if (world.isClientSide && player.isShiftKeyDown()) {
			Book book = ItemStackUtil.getBookFromStack(bookStack);

			if (book != null) {
				Pair<BookEntry, Integer> hover = getHoveredEntry(book);
				if (hover != null) {
					int page = hover.getSecond() * 2;
					book.getContents().setTopEntry(hover.getFirst().getId(), page);
				}
			}
		}
		return InteractionResult.PASS;
	}

	@Nullable
	private static Pair<BookEntry, Integer> getHoveredEntry(Book book) {
		Minecraft mc = Minecraft.getInstance();
		HitResult res = mc.hitResult;
		if (mc.level != null && res instanceof BlockHitResult hit) {
			BlockPos pos = hit.getBlockPos();
			BlockState state = mc.level.getBlockState(pos);
			Block block = state.getBlock();
			ItemStack picked = block.getCloneItemStack(mc.level, pos, state);

			if (!picked.isEmpty()) {
				return book.getContents().getEntryForStack(picked);
			}
		}

		return null;
	}

}
