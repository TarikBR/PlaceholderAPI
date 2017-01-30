package me.rojo8399.placeholderapi.expansions;

import java.util.Optional;
import java.util.function.Function;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializer;

public interface Expansion {

	/**
	 * If any requirements are required to be checked before this hook can
	 * register, add them here
	 * 
	 * @return true if this hook meets all the requirements to register
	 */
	public boolean canRegister();

	/**
	 * Get the identifier that this placeholder expansion uses to be passed
	 * placeholder requests
	 * 
	 * @return placeholder identifier that is associated with this class
	 */
	public String getIdentifier();

	/**
	 * Get the author of this PlaceholderExpansion
	 * 
	 * @return name of the author for this expansion
	 */
	public String getAuthor();

	/**
	 * Get the version of this PlaceholderExpansion
	 * 
	 * @return current version of this expansion
	 */
	public String getVersion();

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a text. If strings need to be
	 *         converted to text, use the parser.
	 */
	public Text onPlaceholderRequest(Player player, Optional<String> token, Function<String, Text> textParser);

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a string serialized with the
	 *         serializer
	 */
	public default String onPlaceholderRequestLegacy(Player player, Optional<String> token, TextSerializer serializer) {
		return onPlaceholderRequestLegacy(player, token, serializer::serialize);
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a string
	 */
	public default String onPlaceholderRequestLegacy(Player player, Optional<String> token) {
		return onPlaceholderRequestLegacy(player, token, Text::toPlain);
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a string created with the text parser
	 */
	public default String onPlaceholderRequestLegacy(Player player, Optional<String> token,
			Function<Text, String> textParser) {
		Text t = onPlaceholderRequest(player, token);
		if(t==null) {
			return null;
		}
		return textParser.apply(t);
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a text. If strings need to be
	 *         converted to text, use the serializer.
	 */
	public default Text onPlaceholderRequest(Player player, Optional<String> token, TextSerializer serializer) {
		return onPlaceholderRequest(player, token, serializer::deserialize);
	}

	/**
	 * Parse the token for the player
	 * 
	 * @return the result of the parse as a text. If strings need to be
	 *         converted to text, use Text.of().
	 */
	public default Text onPlaceholderRequest(Player player, Optional<String> token) {
		return onPlaceholderRequest(player, token, Text::of);
	}

}
