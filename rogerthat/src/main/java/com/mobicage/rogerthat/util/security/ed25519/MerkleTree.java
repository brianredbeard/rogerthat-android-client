/*
 * Copyright 2018 GIG Technology NV
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @@license_version:1.4@@
 */

package com.mobicage.rogerthat.util.security.ed25519;

import org.spongycastle.jcajce.provider.digest.Blake2b;


import java.util.ArrayList;
import java.util.List;

/**
 * Copied from https://github.com/quux00/merkle-tree
 * The MIT License (MIT)
 * Copyright (c) 2015 Michael Peterson
 */
public class MerkleTree {

    public static final int MAGIC_HDR = 0xcdaace99;
    public static final int INT_BYTES = 4;
    public static final int LONG_BYTES = 8;
    public static final byte LEAF_SIG_TYPE = 0x0;
    public static final byte INTERNAL_SIG_TYPE = 0x01;

    private List<byte[]> leafSigs;
    private Node root;
    private int depth;
    private int nnodes;

    /**
     * Use this constructor to create a MerkleTree from a list of leaf signatures.
     * The Merkle tree is built from the bottom up.
     * @param leafSignatures
     */
    public MerkleTree(List<byte[]> leafSignatures) {
        constructTree(leafSignatures);
    }

    /**
     * Create a tree from the bottom up starting from the leaf signatures.
     * @param signatures
     */
    void constructTree(List<byte[]> signatures) {
        if (signatures.size() <= 1) {
            throw new IllegalArgumentException("Must be at least two signatures to construct a Merkle tree");
        }

        leafSigs = signatures;
        nnodes = signatures.size();
        List<Node> parents = bottomLevel(signatures);
        nnodes += parents.size();
        depth = 1;

        while (parents.size() > 1) {
            parents = internalLevel(parents);
            depth++;
            nnodes += parents.size();
        }

        root = parents.get(0);
    }

    public Node getRoot() {
        return root;
    }

    public int getHeight() {
        return depth;
    }


    /**
     * Constructs an internal level of the tree
     */
    List<Node> internalLevel(List<Node> children) {
        List<Node> parents = new ArrayList<Node>(children.size() / 2);

        for (int i = 0; i < children.size() - 1; i += 2) {
            Node child1 = children.get(i);
            Node child2 = children.get(i+1);

            Node parent = constructInternalNode(child1, child2);
            parents.add(parent);
        }

        if (children.size() % 2 != 0) {
            Node child = children.get(children.size()-1);
            Node parent = constructInternalNode(child, null);
            parents.add(parent);
        }

        return parents;
    }


    /**
     * Constructs the bottom part of the tree - the leaf nodes and their
     * immediate parents.  Returns a list of the parent nodes.
     */
    List<Node> bottomLevel(List<byte[]> signatures) {
        List<Node> parents = new ArrayList<>(signatures.size() / 2);

        for (int i = 0; i < signatures.size() - 1; i += 2) {
            Node leaf1 = constructLeafNode(signatures.get(i));
            Node leaf2 = constructLeafNode(signatures.get(i+1));

            Node parent = constructInternalNode(leaf1, leaf2);
            parents.add(parent);
        }

        // if odd number of leafs, handle last entry
        if (signatures.size() % 2 != 0) {
            Node leaf = constructLeafNode(signatures.get(signatures.size() - 1));
            Node parent = constructInternalNode(leaf, null);
            parents.add(parent);
        }

        return parents;
    }

    private Node constructInternalNode(Node child1, Node child2) {
        Node parent = new Node();
        parent.type = INTERNAL_SIG_TYPE;

        if (child2 == null) {
            parent.sig = child1.sig;
        } else {
            parent.sig = internalHash(child1.sig, child2.sig);
        }

        parent.left = child1;
        parent.right = child2;
        return parent;
    }

    private static Node constructLeafNode(byte[] signature) {
        Node leaf = new Node();
        leaf.type = LEAF_SIG_TYPE;
        leaf.sig = signature;
        return leaf;
    }

    byte[] internalHash(byte[] leftChildSig, byte[] rightChildSig) {
        Blake2b.Blake2b256 digest = new Blake2b.Blake2b256();
        digest.reset();
        digest.update(concatHashes(leftChildSig, rightChildSig));
        return digest.digest();
    }

    public static byte[] concatHashes(byte[] leftHash, byte[] rightHash) {
        byte[] newHash = new byte[1 + leftHash.length + rightHash.length];
        newHash[0] = 1;
        System.arraycopy(leftHash, 0, newHash, 1, leftHash.length);
        System.arraycopy(rightHash, 0, newHash, leftHash.length + 1, rightHash.length);
        return newHash;
    }

    /* ---[ Node class ]--- */

    /**
     * The Node class should be treated as immutable, though immutable
     * is not enforced in the current design.
     *
     * A Node knows whether it is an internal or leaf node and its signature.
     *
     * Internal Nodes will have at least one child (always on the left).
     * Leaf Nodes will have no children (left = right = null).
     */
    static class Node {
        public byte type;  // INTERNAL_SIG_TYPE or LEAF_SIG_TYPE
        public byte[] sig; // signature of the node
        public Node left;
        public Node right;

        @Override
        public String toString() {
            String leftType = "<null>";
            String rightType = "<null>";
            if (left != null) {
                leftType = String.valueOf(left.type);
            }
            if (right != null) {
                rightType = String.valueOf(right.type);
            }
            return String.format("MerkleTree.Node<type:%d, sig:%s, left (type): %s, right (type): %s>",
                    type, sigAsString(), leftType, rightType);
        }

        private String sigAsString() {
            StringBuffer sb = new StringBuffer();
            sb.append('[');
            for (int i = 0; i < sig.length; i++) {
                sb.append(sig[i]).append(' ');
            }
            sb.insert(sb.length()-1, ']');
            return sb.toString();
        }
    }
}
