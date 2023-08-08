package com.example.cinemaroom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
public class CinemaRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(CinemaRoomApplication.class, args);
    }
}

@RestController
class MovieController {
    private MovieRoom movieRoom;
    private List<ResponsePurchase> purchasedTickets = new ArrayList<>();
    private int currentIncome;

    @PostConstruct
    private void init() {
        List<AvailableSeats> seats = new ArrayList<>();
        int rows = 9;
        int columns = 9;
        for (int row = 1; row <= rows; row++) {
            for (int column = 1; column <= columns; column++) {
                int price = row <= 4 ? 10 : 8;
                seats.add(new AvailableSeats(row, column, price, false));
            }
        }

        movieRoom = new MovieRoom(rows, columns, seats);
    }

    @GetMapping("/seats")
    public MovieRoom getMovieRoom() {
        return movieRoom;
    }

    @PostMapping("/purchase")
    public ResponsePurchase buyTicket (@RequestBody(required = true) Map<String, Integer> requestBody) {
        int row = requestBody.getOrDefault("row", -1);
        int column = requestBody.getOrDefault("column", -1);

        if(row <= 0 || column <= 0 || row > this.movieRoom.getTotal_rows() || column > this.movieRoom.getTotal_columns()) {
            throw new PurchaseSeatResponseException("The number of a row or a column is out of bounds!");
        }
        List<AvailableSeats> seats = movieRoom.getAvailable_seats();
        for(AvailableSeats seat : seats) {
            if(seat.getRow() == row && seat.getColumn() == column) {
                if(seat.isTaken()) {
                    throw new PurchaseSeatResponseException("The ticket has been already purchased!");
                }
                seat.setTaken(true);
                UUID id = UUID.randomUUID();
                ResponsePurchase purchase = new ResponsePurchase(id, seat);
                currentIncome += seat.getPrice();
                purchasedTickets.add(purchase);
                return purchase;
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found!");
    }

    @PostMapping("/return")
    public ResponseReturn returnTicket(@RequestBody(required = true) Map<String, String> requestBody) {
        String token = requestBody.getOrDefault("token", "");
        UUID tokenUUID = UUID.fromString(token);

        for(int i = 0; i < purchasedTickets.size(); i++) {
            ResponsePurchase ticket = purchasedTickets.get(i);
            if(ticket.getToken().equals(tokenUUID)) {
                int row = ticket.getTicket().getRow();
                int column = ticket.getTicket().getColumn();

                for(AvailableSeats seat : movieRoom.getAvailable_seats()) {
                    if(seat.getRow() == row && seat.getColumn() == column) {
                        seat.setTaken(false);
                        currentIncome -= seat.getPrice();
                        purchasedTickets.remove(i);
                        return new ResponseReturn(seat);
                    }
                }

            }
        }

        throw new ReturnTicketResponseException("Wrong token!");
    }

    @GetMapping("/stats")
    public ResponseStats getStats(@RequestParam(required = true) String password) {
        if(password.equals("super_secret")) {
            int availableSeats = 81 - purchasedTickets.size();

            return new ResponseStats(currentIncome, availableSeats, purchasedTickets.size());
        }

        throw new ResponseStatsException("The password is wrong!");
    }

}

class MovieRoom {
    private int total_rows;
    private int total_columns;
    private List<AvailableSeats> available_seats = new ArrayList<>();

    public MovieRoom() {}
    public MovieRoom(int total_rows, int total_columns, List<AvailableSeats> available_seats) {
        this.total_columns = total_columns;
        this.total_rows = total_rows;
        this.available_seats = available_seats;
    }

    public int getTotal_rows() {
        return total_rows;
    }

    public void setTotal_rows(int total_rows) {
        this.total_rows = total_rows;
    }

    public int getTotal_columns() {
        return total_columns;
    }

    public void setTotal_columns(int total_columns) {
        this.total_columns = total_columns;
    }

    public List<AvailableSeats> getAvailable_seats() {
        return available_seats;
    }

}

class AvailableSeats {
    private int row;
    private int column;
    private int price;
    @JsonIgnore
    private boolean taken;
    @JsonIgnore
    private boolean id;

    public AvailableSeats() {}

    public AvailableSeats(int row, int column, int price, boolean taken) {
        this.row = row;
        this.column = column;
        this.price = price;
        this.taken = taken;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }
}

class ResponsePurchase {
    private UUID token;
    private AvailableSeats ticket;

    public ResponsePurchase() {}
    public ResponsePurchase(UUID token, AvailableSeats ticket) {
        this.token = token;
        this.ticket = ticket;
    }

    public UUID getToken() {
        return this.token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public AvailableSeats getTicket() {
        return this.ticket;
    }

    public void setTicket(AvailableSeats ticket) {
        this.ticket = ticket;
    }
}

class ResponseReturn {
    private AvailableSeats returned_ticket;

    public ResponseReturn() {}
    public ResponseReturn(AvailableSeats returned_ticket) {
        this.returned_ticket = returned_ticket;
    }

    public void setReturned_ticket(AvailableSeats returned_ticket) {
        this.returned_ticket = returned_ticket;
    }

    public AvailableSeats getReturned_ticket() {
        return this.returned_ticket;
    }
}

class ResponseStats {
    private int current_income;
    private int number_of_available_seats;
    private int number_of_purchased_tickets;

    public ResponseStats() {}
    public ResponseStats(int current_income, int number_of_available_seats, int number_of_purchased_tickets) {
        this.current_income = current_income;
        this.number_of_available_seats = number_of_available_seats;
        this.number_of_purchased_tickets = number_of_purchased_tickets;
    }

    public int getCurrent_income() {
        return current_income;
    }

    public void setCurrent_income(int current_income) {
        this.current_income = current_income;
    }

    public int getNumber_of_available_seats() {
        return number_of_available_seats;
    }

    public void setNumber_of_available_seats(int number_of_available_seats) {
        this.number_of_available_seats = number_of_available_seats;
    }

    public int getNumber_of_purchased_tickets() {
        return number_of_purchased_tickets;
    }

    public void setNumber_of_purchased_tickets(int number_of_purchased_tickets) {
        this.number_of_purchased_tickets = number_of_purchased_tickets;
    }
}