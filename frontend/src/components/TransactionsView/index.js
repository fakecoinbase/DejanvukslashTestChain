import React, { Component } from 'react';

import { BASE_URL } from '../../Constants';

import Pages from '../Pages';

import Transaction from '../Transaction';

import { withRouter } from 'react-router-dom';

import './TxView.css';

class TransactionsView extends Component {
    controller = new AbortController();

    constructor(props) {
        super(props);
        this.state = {
            transactions: null,
            currentPage: 1
        }
    }

    handleClick(page) {
        this.setState({
            currentPage: Number(page)
        });
    }

    async componentDidMount() {
        await fetch(BASE_URL + 'transaction', {
            signal: this.controller.signal,
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(transactions => {
            this.setState({ transactions });
        });
    }

    componentWillUnmount() {
        this.controller.abort();
    }


    render() {

        const { transactions, currentPage } = this.state;

        if (transactions != null) {
            const indexLastTx = currentPage * 30;
            const indexFirstTx = indexLastTx - 30;
            const currTxs = transactions.slice(indexFirstTx, indexLastTx);

            const txRows = currTxs.map((tx, index) =>
                (
                    <Transaction key={index} {...tx}></Transaction>
                )
            )

            return (
                <div className="transactions">
                    <div className="blockchain-table">
                        <h5> Unconfirmed Transactions </h5>

                        <Pages currentPage={currentPage} perRow={30} transactionsLength={transactions.length} handleClick={this.handleClick.bind(this)}></Pages>

                        {txRows}
                    </div>

                </div>
            );
        }
        else return <div> LOADING </div>
    }
}

export default withRouter(TransactionsView);