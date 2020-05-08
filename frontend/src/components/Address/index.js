import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import { Link } from 'react-router-dom';

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
            }
        }
    }

    componentDidMount() {
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
            await this.fetchBlockData(this.props.match.params.publicKey);
        }
    }

    render() {

        const { userTransactions } = this.state;

        const sentTransactionRows = userTransactions.sentTransactions.map((tx, index) => 
            <Transaction key={index} {...tx} isSent={false}></Transaction>
        );

        const receivedTransactionRows = userTransactions.receivedTransactions.map((tx, index) => 
            <Transaction key={index} {...tx} isSent={true}></Transaction>
        );

        return (
            <div className="address">
                <h5 id="h5-user-transactions"> Sent Transactions </h5>

                {sentTransactionRows}


                <h5 id="h5-user-transactions"> Received Transactions </h5>

                {receivedTransactionRows}
            </div>

        );
    }
}

export default withRouter(Address);