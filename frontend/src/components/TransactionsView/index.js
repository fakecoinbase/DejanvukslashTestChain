import React, { Component } from 'react';

import { BASE_URL } from '../../Constants';

import Pages from '../Pages';

import Transaction from '../Transaction';


import { withRouter } from 'react-router-dom';

import './TxView.css';

class TransactionsView extends Component {
    constructor(props) {
        super(props);
        this.state = {
            transactions: [],
            currentPage: 1
        }
    }

    handleClick(page) {
        this.setState({
            currentPage: Number(page)
        });
    }

    async componentWillMount() {
        await fetch(BASE_URL + 'transaction', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(transactions => {
            this.setState({ transactions });
        });
    }


    render() {

        const { transactions, currentPage } = this.state;
        
        const indexLastTx = currentPage * 30;
        const indexFirstTx = indexLastTx - 30;
        const currTxs = transactions.slice(indexFirstTx, indexLastTx);

        const txRows = currTxs.map((tx,index) =>
            (
                <Transaction key={index} {...tx} confirmed={false}></Transaction>
            )
        )

        return (
            <div className="transactions">
                <div className="blockchain-table">
                    <h5> Unconfirmed Transactions </h5>

                    <Pages perRow = {30} transactionsLength={transactions.length} handleClick={this.handleClick.bind(this)}></Pages>

                    {txRows}
                </div>

            </div>
        );
    }
}

export default withRouter(TransactionsView);