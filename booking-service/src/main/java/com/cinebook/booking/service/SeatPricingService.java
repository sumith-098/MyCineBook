package com.cinebook.booking.service;

import com.cinebook.booking.dto.SeatQuoteDto;
import com.cinebook.booking.dto.ShowtimeInfoResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** Direct Java port of the Flask app's get_seat_price() row->category->price lookup. */
@Service
public class SeatPricingService {

    private static final BigDecimal DEFAULT_PRICE = BigDecimal.valueOf(150);

    public List<SeatQuoteDto> priceSeats(ShowtimeInfoResponse info, List<String> seats) {
        return seats.stream().map(seat -> priceOne(info, seat)).toList();
    }

    private SeatQuoteDto priceOne(ShowtimeInfoResponse info, String seat) {
        if (info.getLayout() != null && info.getLayout().getRows() != null && !seat.isBlank()) {
            String rowLetter = seat.substring(0, 1).toUpperCase();
            for (var row : info.getLayout().getRows()) {
                if (row.getLabel() != null && row.getLabel().toUpperCase().equals(rowLetter)) {
                    String category = row.getCategory() == null || row.getCategory().isBlank() ? "Normal" : row.getCategory();
                    var catPrice = info.getCatPrices() == null ? null : info.getCatPrices().get(category);
                    if (catPrice != null && catPrice.getPrice() != null) {
                        return new SeatQuoteDto(seat, category, catPrice.getPrice());
                    }
                    return new SeatQuoteDto(seat, category, DEFAULT_PRICE);
                }
            }
        }
        return new SeatQuoteDto(seat, "Normal", DEFAULT_PRICE);
    }
}
