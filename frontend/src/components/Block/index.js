import React, { Component } from 'react';

import { withRouter } from 'react-router-dom';

import Form from 'react-bootstrap/Form';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

import Transaction from '../Transaction';
import Pages from '../Pages';

import { Link } from 'react-router-dom';

import './Block.css';

class Block extends Component {
    constructor(props) {
        super(props);
        this.state = {
            block: {
                index: 0,
                hash: "",
                previousHash: "",
                timestamp: 0,
                transactions: [],
                merkleRoot: "",
                difficultyTarget: 0,
                nonce: 0
            },
            currentPage: 1
        }
    }

    async componentWillMount() {
        await this.fetchBlockData(this.props.match.params.hash);
    }

    async fetchBlockData(hash) {
        await fetch('http://localhost:8080/' + 'block/' + hash, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(block => {
            this.setState({ block });
        });
    }

    async componentDidUpdate(prevProps) {
        if(prevProps.match.params.hash !== this.props.match.params.hash){
            await this.fetchBlockData(this.props.match.params.hash);
        }
    }

    handleClick(page) {
        this.setState({
            currentPage: Number(page)
        });
    }

    render() {

        const { index, hash, previousHash, timestamp, transactions, merkleRoot, difficultyTarget, nonce } = this.state.block;
        const { currentPage } = this.state;

        const indexLastTx = currentPage * 20;
        const indexFirstTx = indexLastTx - 20;
        const currTxs = transactions.slice(indexFirstTx, indexLastTx);

        const txRows = currTxs.map((tx, index) =>
            (
                <Transaction key={index} {...tx} ></Transaction>
            )
        )

        return (
            <div className="block">
                <Form>
                    <Form.Group as={Row} controlId="formPlaintextIndex">
                        <Form.Label column sm="2">
                            Index
                        </Form.Label>
                        <Col sm="10">
                            {index}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Hash
                        </Form.Label>
                        <Col sm="10">
                            {hash}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Previous Block
                        </Form.Label>
                        <Col sm="10">
                        <Link to={"/block/"+ previousHash} > {previousHash} </Link>
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Timestamp
                        </Form.Label>
                        <Col sm="10">
                            {timestamp}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Merkle Root
                        </Form.Label>
                        <Col sm="10">
                            {merkleRoot}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Difficulty Target
                        </Form.Label>
                        <Col sm="10">
                            {difficultyTarget}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Nonce
                        </Form.Label>
                        <Col sm="10">
                            {nonce}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextEmail">
                        <Form.Label column sm="2">
                            Nr of transactions
                        </Form.Label>
                        <Col sm="10">
                            {transactions.length}
                        </Col>
                    </Form.Group>
                </Form>

                <div className="blockchain-table">
                    <h5 id="h5-block-transactions"> Block's transactions </h5>

                    <Pages perRow={20} transactionsLength={transactions.length} handleClick={this.handleClick.bind(this)} ></Pages>

                    {txRows}
                </div>

            </div>
        );
    }
}

export default withRouter(Block);