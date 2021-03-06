package org.hyperledger.indy.sdk.signus;

import org.hyperledger.indy.sdk.IndyIntegrationTestWithPoolAndSingleWallet;
import org.hyperledger.indy.sdk.InvalidStateException;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;


public class KeyForDidTest extends IndyIntegrationTestWithPoolAndSingleWallet {

	@Test
	public void testKeyForDidWorksForMyDid() throws Exception {
		SignusResults.CreateAndStoreMyDidResult result = Signus.createAndStoreMyDid(wallet, "{}").get();
		String did = result.getDid();
		String key = result.getVerkey();

		String receivedKey = Signus.keyForDid(pool, wallet, did).get();
		assertEquals(key, receivedKey);
	}

	@Test
	public void testKeyForDidWorksForTheirDid() throws Exception {
		Signus.storeTheirDid(wallet, String.format(IDENTITY_JSON_TEMPLATE, DID_MY1, VERKEY_MY1)).get();

		String receivedKey = Signus.keyForDid(pool, wallet, DID_MY1).get();
		assertEquals(VERKEY_MY1, receivedKey);
	}

	@Test
	public void testKeyForDidWorksForGetKeyFromLedger() throws Exception {
		SignusResults.CreateAndStoreMyDidResult result = Signus.createAndStoreMyDid(this.wallet, TRUSTEE_IDENTITY_JSON).get();
		String trusteeDid = result.getDid();

		Signus.storeTheirDid(wallet, String.format(IDENTITY_JSON_TEMPLATE,  DID_MY1, VERKEY_MY1)).get();
		String nymRequest = Ledger.buildNymRequest(trusteeDid,  DID_MY1, VERKEY_MY1, null, null).get();
		Ledger.signAndSubmitRequest(pool, wallet, trusteeDid, nymRequest).get();

		String receivedKey = Signus.keyForDid(pool, wallet, DID_MY1).get();
		assertEquals(VERKEY_MY1, receivedKey);
	}

	@Test
	public void testKeyForDidWorksForNoKey() throws Exception {
		thrown.expect(ExecutionException.class);
		thrown.expectCause(isA(InvalidStateException.class));

		Signus.keyForDid(pool, wallet, DID_MY2).get();
	}
}