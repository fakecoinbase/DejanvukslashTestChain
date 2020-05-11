import React, { Component } from 'react';

import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import InputGroup from 'react-bootstrap/InputGroup';
import FormControl from 'react-bootstrap/FormControl';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';
import ListGroup from 'react-bootstrap/ListGroup';
import Table from 'react-bootstrap/Table';
import { Link } from 'react-router-dom';

import Transaction from '../Transaction';

import './Explorer.css';

class Explorer extends Component {
    constructor(props) {
        super(props);
        this.state = {
            blocks: [],
            txs: [],
            difficulty: 0,
            price: 0,
            mode: 'blocks' // blocks,transactions,difficulty,price
        }
    }

    async componentWillMount() {
        await fetch('http://localhost:8080/' + 'block', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(blocks => {
            this.setState({ blocks });
        });
        
        await fetch('http://localhost:8080/' + 'transaction', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        }).then(response => response.json()).then(txs => {
            this.setState({ txs });
        });
    }

    handleClick(name) {
        let { mode } = this.state;
        mode = name;
        this.setState({ mode });
    }

    tableMode() {
        const { blocks, txs, difficulty, price, mode } = this.state;

        if (mode == 'blocks') {
            return (
                <div className="blocks-table">
                    <h5> Latest blocks </h5>
                    <Link to={"/blockchain"}><h3>View all blocks</h3></Link>
                    <Table responsive>
                        <thead>
                            <tr>
                                <th>Height</th>
                                <th>Hash</th>
                                <th>Timestamp</th>
                                <th>Difficulty</th>
                                <th>Transactions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {
                                blocks.slice(-15).reverse().map(block =>
                                    (
                                        <tr key={block.index}>
                                            <td><Link className="" to={"block/" + block.hash}>{block.index}</Link></td>
                                            <td><Link className="" to={"block/" + block.hash}>{block.hash.substring(0, 1) + "..." + block.hash.substring(20, 50) + '...'}</Link></td>
                                            <td>{new Date(block.timestamp).toUTCString()}</td>
                                            <td>{block.difficultyTarget}</td>
                                            <td>{block.transactions.length}</td>
                                        </tr>
                                    )
                                )
                            }
                        </tbody>
                    </Table>
                </div>
            );
        }
        else if (mode == 'transactions') {

            const txRows = txs.slice(-15).reverse().map(tx =>
                (
                    <Transaction key={tx.txid} {...tx} isSent={true}></Transaction>
                )
            )

            return (
                <div className="transactions-table">
                    <h5> Transactions </h5>
                    <Link to={"/tx/view"}><h3>View all transactions</h3></Link>
                    { txRows }
                </div>
            );
        }
        else if (mode == 'difficulty') {
            return (
                <div id="difficulty-div">
                    <h5> Difficulty </h5>
                    <div> {blocks[blocks.length - 1].difficultyTarget} </div>
                </div>
            );
        }
        else if (mode == 'price') {

        }
    }


    render() {
        const { blocks, txs, difficulty, price, mode } = this.state;

        return (
            <div className="explorer">
                <div className="container ">
                    <div className="row">
                        <div className="col">
                            <h5 id="explorer-h5">Explorer</h5>
                        </div>
                    </div>

                    <div className="row">
                        <div className="col">
                            <h5> Bitcoin </h5>
                            <ListGroup>
                                <ListGroup.Item action active={mode == 'blocks'} name="blocks" onClick={this.handleClick.bind(this, 'blocks')} >Blocks</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'transactions'} name="transactions" onClick={this.handleClick.bind(this, 'transactions')} >Transactions</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'difficulty'} name="difficulty" onClick={this.handleClick.bind(this, 'difficulty')} >Difficulty</ListGroup.Item>
                                <ListGroup.Item action active={mode == 'price'} name="price" onClick={this.handleClick.bind(this, 'price')} >Price</ListGroup.Item>
                            </ListGroup>
                        </div>
                        <div className="col-9">
                            {this.tableMode()}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Explorer;