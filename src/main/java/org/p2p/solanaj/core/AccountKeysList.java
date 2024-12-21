package org.p2p.solanaj.core;

import java.util.*;

public class AccountKeysList {
    private final Map<String, AccountMeta> accounts;

    public AccountKeysList() {
        accounts = new LinkedHashMap<>();
    }

    public void add(AccountMeta accountMeta) {
        String key = accountMeta.getPublicKey().toString();
        accounts.merge(key, accountMeta, (existing, newMeta) ->
                new AccountMeta(existing.getPublicKey(),
                        existing.isSigner() || newMeta.isSigner(),
                        existing.isWritable() || newMeta.isWritable()));
    }

    public void addAll(AccountKeysList metas) {
        metas.accounts.values().forEach(this::add);
    }

    public void addAll(Collection<AccountMeta> metas) {
        metas.forEach(this::add);
    }

    /** Retrieve account list sorted by signer/writable attributes. */
    public ArrayList<AccountMeta> getList() {
        ArrayList<AccountMeta> accountKeysList = new ArrayList<>(accounts.values());
        accountKeysList.sort(metaComparator);
        return accountKeysList;
    }

    private static final Comparator<AccountMeta> metaComparator = Comparator
        .comparing(AccountMeta::isSigner)
        .thenComparing(AccountMeta::isWritable).reversed();
}
