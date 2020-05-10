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

    controller = new AbortController();

    constructor(props) {
        super(props);
        this.state = {
            block: null,
            currentPage: 1
        }
    }


    async componentDidMount() {
        this.fetchBlockData(this.props.match.params.hash);
    }

    componentWillUnmount() {
        this.controller.abort();
    }

    async fetchBlockData(hash) {
        await fetch('http://localhost:8080/' + 'block/' + hash, {
            signal: this.controller.signal,
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
        if (prevProps.match.params.hash !== this.props.match.params.hash) {
            await this.fetchBlockData(this.props.match.params.hash);
        }
    }

    handleClick(page) {
        this.setState({
            currentPage: Number(page)
        });
    }

    render() {

        if (this.state.block != null) {
            const { index, hash, previousHash, timestamp, transactions, merkleRoot, difficultyTarget, nonce } = this.state.block;
            const { currentPage } = this.state;

            const indexLastTx = currentPage * 20;
            const indexFirstTx = indexLastTx - 20;
            const currTxs = transactions.slice(indexFirstTx, indexLastTx);

            const txRows = currTxs.map((tx, index) =>
                (
                    <Transaction key={index} {...tx} isSent={true}></Transaction>
                )
            )

            return (
                <div className="block">
                    <Form className="block-form">
                        <Form.Group as={Row} controlId="formPlaintextIndex">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Index </span>
                            </Form.Label>
                            <Col sm="9">
                                {index}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Hash </span>
                            </Form.Label>
                            <Col sm="9">
                                {hash}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Previous Block </span>
                            </Form.Label>
                            <Col sm="9">
                                <Link to={"/block/" + previousHash} > {previousHash} </Link>
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Timestamp </span>
                            </Form.Label>
                            <Col sm="9">
                                {new Date(timestamp).toGMTString()}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Merkle Root </span>
                            </Form.Label>
                            <Col sm="9">
                                {merkleRoot}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Difficulty </span>
                            </Form.Label>
                            <Col sm="9">
                                {difficultyTarget}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Nonce </span>
                            </Form.Label>
                            <Col sm="9">
                                {nonce}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Nr of transactions </span>
                            </Form.Label>
                            <Col sm="9">
                                {transactions.length}
                            </Col>
                        </Form.Group>

                        <Form.Group as={Row} controlId="formPlaintextEmail">
                            <Form.Label column sm="3">
                                <span className="tx-span"> Block reward </span>
                            </Form.Label>
                            <Col sm="9">
                                {transactions[0].value}
                            </Col>
                        </Form.Group>

                        <hr></hr>
                    </Form>

                    <div className="blockchain-table">
                        <h5 id="h5-block-transactions"> Transactions </h5>

                        <Pages currentPage={currentPage} perRow={20} transactionsLength={transactions.length} handleClick={this.handleClick.bind(this)} ></Pages>

                        {txRows}
                    </div>

                </div>
            );
        }
        else return <p>LOADING</p>
    }
}

export default withRouter(Block);