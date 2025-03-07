package domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GymTicket implements Available {
    private int period;
    private int price;
}
