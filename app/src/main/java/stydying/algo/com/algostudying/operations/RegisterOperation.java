package stydying.algo.com.algostudying.operations;

import android.content.Context;

import stydying.algo.com.algostudying.data.entities.stats.User;
import stydying.algo.com.algostudying.errors.NetworkException;
import stydying.algo.com.algostudying.network.services.UsersService;

/**
 * Created by Anton on 05.02.2016.
 */
public class RegisterOperation implements OperationProcessingService.Operation {

    private String login;
    private String pass;
    private User.Type type;
    private String name;

    public RegisterOperation() {
    }

    public RegisterOperation(String login, String pass, String name, User.Type type) {
        this.login = login;
        this.pass = pass;
        this.type = type;
        this.name = name;
    }

    @Override
    public Object loadFromNetwork(Context context) throws NetworkException {
        UsersService.register(login, pass, name, type).save();
        return null;
    }

    @Override
    public Object loadFromLocal(Context context) {
        return null;
    }

    @Override
    public OperationType type() {
        return OperationType.NETWORK_ONLY;
    }
}
