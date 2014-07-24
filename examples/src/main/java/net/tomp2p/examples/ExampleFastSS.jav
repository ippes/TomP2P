/*
 * Copyright 2009 Thomas Bocek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.tomp2p.examples;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureTracker;
import net.tomp2p.p2p.Peer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class ExampleFastSS {
    public static void main(String[] args) throws Exception {
        PeerDHT[] peers = null;
        try {
            peers = ExampleUtils.createAndAttachPeersDHT(100, 4001);
            ExampleUtils.bootstrap(peers);
            exampleFastSS(peers);
        } finally {
            // 0 is the master
            peers[0].shutdown();
        }
    }

    private static void exampleFastSS(PeerDHT[] peers) throws IOException, ClassNotFoundException {
        final String title = "another great song";
        // key of the file
        final Number160 key = Number160.createHash(title);
        // peer 15 has this song
        peers[15].addTracker(key).start().awaitUninterruptibly();
        // when a peer asks us, we reply with the song
        peers[15].peer().objectDataReply(new ObjectDataReply() {
            @Override
            public Object reply(PeerAddress sender, Object request) throws Exception {
                if (request instanceof Number160 && ((Number160) request).equals(key)) {
                    return title + ": and here comes the mp3 file";
                } else {
                    return null;
                }
            }
        });
        // now prepare for fastss
        for (String word : title.split(" ")) {
            for (String deletion : deletion(word)) {
                Object[] tmp = new Object[] { key, word, deletion };
                peers[15].put(Number160.createHash(deletion)).data(new Data(tmp)).start().awaitUninterruptibly();
            }
        }
        System.out.println("we have indexed [" + title + "]");
        // done, now search for greet
        for (String deletion : deletion("greet")) {
            FutureGet futureGet = peers[20].get(Number160.createHash(deletion)).start().awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                // if we found a match
                Object[] tmp = (Object[]) futureGet.data().object();
                Number160 key1 = (Number160) tmp[0];
                // get the peers that have this file
                FutureTracker futureTracker = peers[20].getTracker(key1).start();
                futureTracker.awaitUninterruptibly();
                PeerAddress peerAddress = futureTracker.trackers().iterator().next().map().keySet().iterator().next();
                // download
                FutureDirect futureDirect = peers[20].peer().sendDirect(peerAddress).object(key1).start();
                futureDirect.awaitUninterruptibly();
                System.out.println("we searched for \"greet\", and found [" + tmp[2] + "], ed(" + tmp[1] + ",greet)="
                        + ld((String) tmp[1], "greet") + ". After downloading we get [" + futureDirect.object() + "]");
            }
        }
    }

    private static Collection<String> deletion(String word) {
        Set<String> resultSet = new HashSet<String>();
        resultSet.add(word);
        StringBuilder sb = new StringBuilder(word);
        for (int i = 0; i < word.length(); i++) {
            char c = sb.charAt(i);
            sb.deleteCharAt(i);
            resultSet.add(sb.toString());
            sb.insert(i, c);
        }
        return resultSet;
    }

    /**
     * Plain good old Leveshtein distance. Code taken from http://en.wikipedia.org/wiki/Levenshtein
     * 
     * @param s
     *            The string1 to compare
     * @param t
     *            The string2 to compare
     * @return The Leveshtein distance
     */
    private static int ld(final String s, final String t) {
        final int n = s.length();
        final int m = t.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        int[][] d = new int[n + 1][m + 1];
        for (int i = 0; i <= n; d[i][0] = i++);
        for (int j = 1; j <= m; d[0][j] = j++);
        for (int i = 1; i <= n; i++) {
            char sc = s.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int v = d[i - 1][j - 1];
                if (t.charAt(j - 1) != sc) {
                    v++;
                }
                d[i][j] = Math.min(Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1), v);
            }
        }
        return d[n][m];
    }
}
