package mbanje.kurt.todo;


import ckm.simple.sql_provider.UpgradeScript;
import ckm.simple.sql_provider.annotation.ProviderConfig;
import ckm.simple.sql_provider.annotation.SimpleSQLConfig;

/**
 * Created by kurt on 2015/09/02.
 */
@SimpleSQLConfig(
        name = Todo.PROVIDER_CLASS,
        authority = "com.peirr.ckm.test_provider",
        database = "test2.db",
        version = 1)
public class Config implements ProviderConfig {
    @Override
    public UpgradeScript[] getUpdateScripts() {
        return new UpgradeScript[0];
    }
}
