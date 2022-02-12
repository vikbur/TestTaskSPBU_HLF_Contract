package vikbur;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import com.owlike.genson.Genson;

import java.util.ArrayList;
import java.util.List;


@Contract(
        name = "MyContract",
        info = @Info(
                title = "MyContract",
                description = "Contract for get|update log",
                version = "0.0.1-SNAPSHOT"))

@Default
public final class MyContract implements ContractInterface{

    private final Genson genson = new Genson();

    /**
     * Creates a new event on the ledger.
     *
     * @param ctx the transaction context
     * @param key the key for the new event
     * @return the created Car
     */
    @Transaction()
    public MyEvent addEvent(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();

        String eventState = stub.getStringState(key);

        if (!eventState.isEmpty()) {
            String errorMessage = String.format("Event %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        MyEvent event = new MyEvent(key);
        eventState = genson.serialize(event);
        stub.putStringState(key, eventState);

        return event;
    }

    /**
     * Retrieves all cars from the ledger.
     *
     * @param ctx the transaction context
     * @return array of Cars found on the ledger
     */
    @Transaction()
    public String queryAllEvents(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        final String startKey = "";
        final String endKey = "*************************************************";

        List<String> queryResults = new ArrayList<>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange(startKey, endKey);

        for (KeyValue result: results) {
            MyEvent event = genson.deserialize(result.getStringValue(), MyEvent.class);
            queryResults.add(result.getKey());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

}
