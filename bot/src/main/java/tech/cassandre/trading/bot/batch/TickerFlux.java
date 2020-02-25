package tech.cassandre.trading.bot.batch;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import tech.cassandre.trading.bot.dto.market.TickerDTO;
import tech.cassandre.trading.bot.service.MarketService;
import tech.cassandre.trading.bot.util.base.BaseFlux;
import tech.cassandre.trading.bot.util.dto.CurrencyPairDTO;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Ticker flux.
 */
@Lazy
@Component
public class TickerFlux extends BaseFlux<TickerDTO> {

	/** Market service. */
	private final MarketService marketService;

	/** Requested tickers. */
	private final List<CurrencyPairDTO> requestedCurrencyPairs = new LinkedList<>();

	/** Last requested currency pair. */
	private CurrencyPairDTO lastRequestedCurrencyPairs = null;

	/**
	 * Constructor.
	 *
	 * @param newMarketService market service.
	 */
	public TickerFlux(final MarketService newMarketService) {
		this.marketService = newMarketService;
	}

	/**
	 * Update the list of requested currency pairs.
	 *
	 * @param newRequestedCurrencyPairs new list of requested currency pairs.
	 */
	public void updateRequestedCurrencyPairs(final Set<CurrencyPairDTO> newRequestedCurrencyPairs) {
		requestedCurrencyPairs.addAll(newRequestedCurrencyPairs);
	}

	@Override
	@SuppressWarnings("unused")
	protected final Set<TickerDTO> getNewValues() {
		getLogger().debug("TickerDTO - Retrieving new values");
		Set<TickerDTO> newValues = new LinkedHashSet<>();
		getCurrencyPairToTreat()
				.flatMap(marketService::getTicker)
				.ifPresent(t -> {
					getLogger().debug("TickerDTO - new ticker received : {}", t);
					newValues.add(t);
				});
		return newValues;
	}

	/**
	 * Returns the next currency pair to test.
	 *
	 * @return currency pair to treat.
	 */
	private Optional<CurrencyPairDTO> getCurrencyPairToTreat() {
		// TODO Optimize this.
		final CurrencyPairDTO nextCurrencyPairToTreat;

		// If none is required.
		if (requestedCurrencyPairs.isEmpty()) {
			return Optional.empty();
		}
		if (lastRequestedCurrencyPairs == null) {
			// If none has been retrieved.
			nextCurrencyPairToTreat = requestedCurrencyPairs.get(0);
		} else {
			// We get the position of the last requested currency pair.
			int position = requestedCurrencyPairs.indexOf(lastRequestedCurrencyPairs);
			if (position == requestedCurrencyPairs.size() - 1) {
				// We are at the last of the list, go back to first element.
				nextCurrencyPairToTreat = requestedCurrencyPairs.get(0);
			} else {
				// We take the next one.
				nextCurrencyPairToTreat = requestedCurrencyPairs.get(position + 1);
			}
		}
		lastRequestedCurrencyPairs = nextCurrencyPairToTreat;
		return Optional.of(nextCurrencyPairToTreat);
	}

}