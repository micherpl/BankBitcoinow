package models;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;


@Entity
@Table(name = "transaction")
public class Transaction {
    private Long id;
    private Long address_id;
    private String source_address;
    private String destination_address;
    private BigDecimal amount;
    private Timestamp created_at;
    private int status;
    private int confirmations;
    private byte blockchain_data;

    @ManyToOne
    @JoinColumn(name = "id")
    private Address address;


}
