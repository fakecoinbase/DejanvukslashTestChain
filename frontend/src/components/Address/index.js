import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';

import Form from 'react-bootstrap/Form';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';

import Pages from '../Pages';

import { withRouter } from 'react-router-dom';

import Transaction from '../Transaction';

import './Address.css';

class Address extends Component {
    constructor(props) {
        super(props);
        this.state = {
            userTransactions: {
                sentTransactions: [],
                receivedTransactions: []
            },
            currentPageSent: 1,
            currentPageReceived: 1
        }
    }

    handleClickSent(page) {
        this.setState({
            currentPageSent: Number(page)
        });
    }

    handleClickReceived(page) {
        this.setState({
            currentPageReceived: Number(page)
        });
    }


    componentWillMount() {
        this.fetchUserTransactions(this.props.match.params.publicKey);
    }

    async fetchUserTransactions(publicKey) {
        await fetch('http://localhost:8080/address/foo?walletPublicKey=' + publicKey, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(userTransactions => {
            this.setState({ userTransactions });
        });
    }

    async componentDidUpdate(prevProps) {
        if(prevProps.match.params.publicKey !== this.props.match.params.publicKey){
            await this.fetchUserTransactions(this.props.match.params.publicKey);
        }
    }

    calculatePageTransaction(currentPage,nrPerPage,transactions) {
        const indexLastTx = currentPage * nrPerPage;
        const indexFirstTx = indexLastTx - nrPerPage;
        const currTxs = transactions.slice(indexFirstTx, indexLastTx);
        return currTxs;
    }

    render() {

        const { userTransactions, currentPageSent, currentPageReceived } = this.state;

        const currSentTxs = this.calculatePageTransaction(currentPageSent, 10, userTransactions.sentTransactions);
        const currReceivedTxs = this.calculatePageTransaction(currentPageReceived, 10, userTransactions.receivedTransactions);

        const sentTransactionRows = currSentTxs.map((tx, index) => 
            <Transaction key={index} {...tx} isSent={false}></Transaction>
        );

        const receivedTransactionRows = currReceivedTxs.map((tx, index) => 
            <Transaction key={index} {...tx} isSent={true}></Transaction>
        );

        return (
            <div className="address">
                <Form>
                    <Form.Group as={Row} controlId="formPlaintextIndex">
                        <Form.Label column sm="2">
                            Address:
                        </Form.Label>
                        <Col sm="10">
                            {this.props.match.params.publicKey}
                        </Col>
                    </Form.Group>

                    <Form.Group as={Row} controlId="formPlaintextIndex">
                        <Form.Label column sm="2">
                            Total balance:
                        </Form.Label>
                        <Col sm="10">
                            {userTransactions.totalBalance}
                        </Col>
                    </Form.Group>
                </Form>


                <h5 className="h5-user-transactions"> Sent Transactions </h5>

                <Pages currentPage={currentPageSent} perRow={10} transactionsLength={userTransactions.sentTransactions.length} handleClick={this.handleClickSent.bind(this)} ></Pages>

                {sentTransactionRows}


                <h5 className="h5-user-transactions"> Received Transactions </h5>

                <Pages currentPage={currentPageReceived} perRow={10} transactionsLength={userTransactions.receivedTransactions.length} handleClick={this.handleClickReceived.bind(this)} ></Pages>

                {receivedTransactionRows}
            </div>

        );
    }
}

export default withRouter(Address);