import React, { Component } from 'react';

import BASE_URL from '../../Constants';

import { withRouter } from 'react-router-dom';

import Form from 'react-bootstrap/Form';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

import { Link } from 'react-router-dom';

import './TxDetails.css';

class TransactionDetails extends Component {
    controller = new AbortController();

    constructor(props) {
        super(props);
        this.state = {
            transaction: null
        }
    }

    componentDidMount() {
        this.fetchTransactionData(this.props.match.params.txid);
        
    }

    componentWillUnmount() {
        this.controller.abort();
    }


    async fetchTransactionData(txid) {
        await fetch('http://localhost:8080/' + 'transaction/' + txid, {
            signal: this.controller.signal,
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(transaction => {
           this.setState({ transaction });
        }).catch(error => {console.log("failed fetch")});
    }

    async componentDidUpdate(prevProps) {
        if (prevProps.match.params.txid !== this.props.match.params.txid) {
            await this.fetchTransactionData(this.props.match.params.txid);
        }
    }

    render() {

        const { transaction } = this.state;

        if(transaction != null) {
            const inputsList = transaction.inputs.map((input, index) => (
                <li className="tx-detail-list" key={index}>
                    <div>Index: {input.index}</div>
                    <div>Address: <Link to={"/tx/"+ input.previousTx} > {input.previousTx} </Link></div>
                </li>
            ));
    
            const outputsList = transaction.outputs.map((output, index) => (
                <li className="tx-detail-list" key={index}>
                    <div>Owner: <Link to={"/address/"+ encodeURIComponent(output.to) }> {output.to + " "} </Link></div>
                    <div>Value: <span id="plus"> {output.value} TC </span></div>
                </li>
            ));
    
            return (
    
                <div className="">
    
                    <Form>
                        <Form.Group as={Row} controlId="formPlaintextTxid">
                            <Form.Label column sm="2">
                                Txid: 
                            </Form.Label>
                            <Col sm="10">
                                {transaction.txid}
                            </Col>
                        </Form.Group>
    
                        <Form.Group as={Row} controlId="formPlaintextTxSender">
                            <Form.Label column sm="2">
                                Sender: 
                            </Form.Label>
                            <Col sm="10">
                                {transaction.sender}
                            </Col>
                        </Form.Group>
    
                        <Form.Group as={Row} controlId="formPlaintextTxReceiver">
                            <Form.Label column sm="2">
                                Receiver: 
                            </Form.Label>
                            <Col sm="10">
                                {transaction.receiver}
                            </Col>
                        </Form.Group>
    
                        <Form.Group as={Row} controlId="formPlaintextTxValue">
                            <Form.Label column sm="2">
                                Value: 
                            </Form.Label>
                            <Col sm="10">
                                {transaction.value}
                            </Col>
                        </Form.Group>
    
                        <Form.Group as={Row} controlId="formPlaintextTxVerified">
                            <Form.Label column sm="2">
                                Verified: 
                            </Form.Label>
                            <Col sm="10">
                            { transaction.verified == true ?  <h3 id="confirmed-tx">CONFIRMED</h3> : <h3 id="unconfirmed-tx">UNCONFIRMED</h3>}
                            </Col>
                        </Form.Group>
    
                        <Form.Group as={Row} controlId="formPlaintextTxOwner">
                            <Form.Label column sm="2">
                                Included in block: 
                            </Form.Label>
                            <Col sm="10">
                                {transaction.ownerBlock == "" ? <h3> MEMPOOL </h3> :
                                 <Link to={"/block/"+ transaction.ownerBlock} > {transaction.ownerBlock} </Link>}
                            </Col>
                        </Form.Group>
                    </Form>

                    <h5 >Inputs: </h5>
    
                    <ul>
                        {inputsList}
                    </ul>
    
                    <hr></hr>

                    <h5>Outputs: </h5>

                    <ul>
                        {outputsList}
                    </ul>
                </div>
            );
        }
        else return (
            <div>LOADING</div>
        );
    }
}

export default withRouter(TransactionDetails);