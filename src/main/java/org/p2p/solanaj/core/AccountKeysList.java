package org.p2p.solanaj.core;

import java.util.*;

public class AccountKeysList {
    private final Map<String, AccountMeta> accounts;

    public AccountKeysList() {
        accounts = new HashMap<>();
    }

    public void add(AccountMeta accountMeta) {
        String key = accountMeta.getPublicKey().toString();
        accounts.merge(key, accountMeta, (existing, newMeta) ->
            !existing.isWritable() && newMeta.isWritable() ? newMeta : existing);
    }

    public void addAll(Collection<AccountMeta> metas) {
        metas.forEach(this::add);
    }

    public ArrayList<AccountMeta> getList() {
        ArrayList<AccountMeta> accountKeysList = new ArrayList<>(accounts.values());
        accountKeysList.sort(metaComparator);
        return accountKeysList;
    }

    private static final Comparator<AccountMeta> metaComparator = Comparator
        .comparing(AccountMeta::isSigner).reversed()
        .thenComparing(AccountMeta::isWritable).reversed();
}
