package authorization;

import java.util.ArrayList;

public class BaseAuthService implements AuthService{

    private class Entry {
        private String login, pass, nick;

        public Entry(String login, String pass, String nick) {
            this.login = login;
            this.pass = pass;
            this.nick = nick;
        }
    }

    private ArrayList<Entry> entries;

    public BaseAuthService() {
        entries = new ArrayList<>();

        //add dummy entries
        entries.add(new Entry("one", "one", "one"));
        entries.add(new Entry("two", "two", "two"));
        entries.add(new Entry("three", "three", "three"));
    }

    public boolean correctPassword(String login, String pass) {
        for (Entry entry : entries) {
            if (entry.login.equals(login) && entry.pass.equals(pass))
                return true;
        }

        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public String getNickByLoginPass(String login, String pass) {
        for (Entry entry :
                entries) {
            if (entry.login.equals(login) && entry.pass.equals(pass))
                return entry.nick;
        }
        return null;
    }

    @Override
    public void stop() {

    }
}
